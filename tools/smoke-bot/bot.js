// Joins a local test server as SmokeBot (an operator, see scripts/smoke-test.sh), then:
//   1. holds a damaged Mending pickaxe and picks up an XP orb -> expects "Mending Repair: +10 (1071/1561)"
//   2. empties its hand and picks up another orb                -> expects "Gain XP: +5"
//   3. runs /ged reload                                         -> expects "Configuration reloaded successfully."
//   4. switches its game language to Thai, picks up an orb      -> expects the Thai message "ได้รับ XP: +5"
//      (per-player-language is on in a fresh config)
// Prints PASS/FAIL/SKIP lines. Exit code 0 = pass, 1 = fail, 3 = this Minecraft version isn't supported by Mineflayer.
//
//   node bot.js <port>
const mineflayer = require('mineflayer')
const nbt = require('prismarine-nbt')

const port = Number(process.argv[2] || 25565)
const sleep = ms => new Promise(resolve => setTimeout(resolve, ms))

function versionAtLeast (version, wanted) {
  const a = version.split('.').map(Number)
  const b = wanted.split('.').map(Number)
  for (let i = 0; i < 3; i++) {
    if ((a[i] || 0) !== (b[i] || 0)) return (a[i] || 0) > (b[i] || 0)
  }
  return true
}

// The item syntax changed in 1.20.5 (components) and again in 1.21.5 (enchantments)
function mendingPickaxe (version) {
  if (!versionAtLeast(version, '1.20.5')) return 'minecraft:diamond_pickaxe{Damage:500,Enchantments:[{id:"minecraft:mending",lvl:1s}]}'
  if (!versionAtLeast(version, '1.21.5')) return 'minecraft:diamond_pickaxe[minecraft:damage=500,minecraft:enchantments={levels:{"minecraft:mending":1}}]'
  return 'minecraft:diamond_pickaxe[minecraft:damage=500,minecraft:enchantments={"minecraft:mending":1}]'
}

const actionBars = []
const chatLines = []

// Text components arrive as JSON strings (older versions) or NBT (1.20.3+). Reduce them to plain text.
function plainText (component) {
  if (component === undefined || component === null) return ''
  if (typeof component === 'string') {
    try { return plainText(JSON.parse(component)) } catch { return component }
  }
  if (component.type && component.value !== undefined) component = nbt.simplify(component)
  if (typeof component !== 'object') return String(component)
  if (Array.isArray(component)) return component.map(plainText).join('')
  const own = component.text !== undefined ? component.text : (component[''] !== undefined ? component[''] : '')
  return String(own) + (component.extra || []).map(plainText).join('')
}

async function waitForActionBar (predicate, timeoutMs, list = actionBars) {
  const end = Date.now() + timeoutMs
  while (Date.now() < end) {
    const match = list.find(predicate)
    if (match) return match
    await sleep(100)
  }
  return null
}

// Summons an XP orb at the bot and waits for a matching action bar; tries a second orb if the first is missed
async function orbAndWait (predicate) {
  for (let attempt = 0; attempt < 2; attempt++) {
    bot.chat('/summon minecraft:experience_orb ~ ~ ~ {Value:5}')
    const match = await waitForActionBar(predicate, 8000)
    if (match) return match
  }
  return null
}

function finish (code, message) {
  console.log(message)
  process.exit(code)
}

setTimeout(() => finish(1, 'FAIL timed out after 90 seconds'), 90000)

const bot = mineflayer.createBot({ host: '127.0.0.1', port, username: 'SmokeBot', auth: 'offline' })

// Read the packets directly: Mineflayer's own actionBar event misses some versions' formats
bot._client.on('action_bar', data => actionBars.push(plainText(data.text)))
bot._client.on('system_chat', data => {
  const text = plainText(data.content || data.formattedMessage)
  if (data.isActionBar || data.overlay || data.type === 2) actionBars.push(text)
  else chatLines.push(text)
})
bot._client.on('chat', data => (data.position === 2 ? actionBars : chatLines).push(plainText(data.message)))
bot.on('kicked', reason => finish(1, `FAIL kicked: ${JSON.stringify(reason)}`))
bot.on('error', error => {
  if (/unsupported|not supported|No data available/i.test(String(error && error.message))) {
    finish(3, `SKIP Mineflayer can't join this version (${error.message})`)
  }
  finish(1, `FAIL ${error && error.stack ? error.stack : error}`)
})

bot.once('spawn', async () => {
  try {
    console.log(`joined as SmokeBot, Minecraft ${bot.version}`)
    await sleep(3000) // let the world settle and the op status arrive
    // Stand on the ground first: an orb summoned while falling can be missed
    for (let i = 0; i < 100 && !bot.entity.onGround; i++) await sleep(100)

    bot.chat('/gamemode survival')
    bot.chat('/clear @s')
    bot.chat(`/give @s ${mendingPickaxe(bot.version)}`) // lands in the selected hotbar slot
    await sleep(1500)
    const mending = await orbAndWait(text => text.startsWith('Mending Repair:'))
    if (!mending) return finish(1, `FAIL no Mending action bar (saw: ${JSON.stringify(actionBars)})`)
    if (!/^Mending Repair: \+\d+ \(\d+\/1561\)$/.test(mending)) return finish(1, `FAIL unexpected Mending text: ${mending}`)
    console.log(`ok   ${mending}`)

    bot.chat('/clear @s')
    await sleep(1500)
    const gain = await orbAndWait(text => text.startsWith('Gain XP:'))
    if (!gain) return finish(1, `FAIL no Gain XP action bar (saw: ${JSON.stringify(actionBars)})`)
    if (gain !== 'Gain XP: +5') return finish(1, `FAIL unexpected Gain XP text: ${gain}`)
    console.log(`ok   ${gain}`)

    bot.chat('/ged reload')
    const reload = await waitForActionBar(text => text.includes('Configuration reloaded successfully'), 8000, chatLines)
    if (!reload) return finish(1, `FAIL /ged reload gave no reply (saw: ${JSON.stringify(chatLines.slice(-5))})`)
    console.log(`ok   ${reload}`)

    // Like a player changing their language in the options menu: no relog
    bot.setSettings({ locale: 'th_th' })
    await sleep(1500)
    const thai = await orbAndWait(text => text.startsWith('ได้รับ XP:'))
    if (!thai) return finish(1, `FAIL no Thai action bar after switching language (saw: ${JSON.stringify(actionBars.slice(-3))})`)
    if (thai !== 'ได้รับ XP: +5') return finish(1, `FAIL unexpected Thai text: ${thai}`)
    console.log(`ok   ${thai}`)

    bot.quit()
    finish(0, 'PASS action bar messages, /ged reload and per-player language work')
  } catch (error) {
    finish(1, `FAIL ${error.stack || error}`)
  }
})
