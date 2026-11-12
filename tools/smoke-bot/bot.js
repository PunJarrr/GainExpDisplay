// Joins a local test server as SmokeBot (an operator, see scripts/smoke-test.sh), then:
//   1. holds a damaged Mending pickaxe and picks up an XP orb -> expects "Mending Repair: +10 (x/1561)"
//   2. empties its hand and picks up another orb                -> expects "Gain XP: +5"
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

async function waitForActionBar (predicate, timeoutMs) {
  const end = Date.now() + timeoutMs
  while (Date.now() < end) {
    const match = actionBars.find(predicate)
    if (match) return match
    await sleep(100)
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
  if (data.isActionBar || data.overlay || data.type === 2) actionBars.push(plainText(data.content || data.formattedMessage))
})
bot._client.on('chat', data => { if (data.position === 2) actionBars.push(plainText(data.message)) })
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

    bot.chat('/gamemode survival')
    bot.chat(`/item replace entity @s weapon.mainhand with ${mendingPickaxe(bot.version)}`)
    await sleep(1500)
    bot.chat('/summon minecraft:experience_orb ~ ~ ~ {Value:5}')
    const mending = await waitForActionBar(text => text.startsWith('Mending Repair:'), 15000)
    if (!mending) return finish(1, `FAIL no Mending action bar (saw: ${JSON.stringify(actionBars)})`)
    if (!/^Mending Repair: \+\d+ \(\d+\/1561\)$/.test(mending)) return finish(1, `FAIL unexpected Mending text: ${mending}`)
    console.log(`ok   ${mending}`)

    bot.chat('/item replace entity @s weapon.mainhand with minecraft:air')
    await sleep(1500)
    bot.chat('/summon minecraft:experience_orb ~ ~ ~ {Value:5}')
    const gain = await waitForActionBar(text => text.startsWith('Gain XP:'), 15000)
    if (!gain) return finish(1, `FAIL no Gain XP action bar (saw: ${JSON.stringify(actionBars)})`)
    if (gain !== 'Gain XP: +5') return finish(1, `FAIL unexpected Gain XP text: ${gain}`)
    console.log(`ok   ${gain}`)

    bot.quit()
    finish(0, 'PASS action bar messages arrived')
  } catch (error) {
    finish(1, `FAIL ${error.stack || error}`)
  }
})
