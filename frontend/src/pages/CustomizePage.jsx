import { useState } from 'react'
import { BookOpen, Check, Lamp, Leaf, RotateCcw, Save, Sparkles } from 'lucide-react'
import roomHero from '../assets/room/bookhaus-room-hero.jpg'
import PageHero from '../components/PageHero'

const defaults = { theme: 'Sunset Haven', lighting: 'Golden Hour', shelves: 'Classic', decor: ['Plants', 'Candles'] }
const options = {
  theme: ['Sunset Haven', 'Moonlight Study', 'Forest Cabin', 'Paris Apartment'],
  lighting: ['Daylight', 'Golden Hour', 'Moonlight', 'Candlelight'],
  shelves: ['Classic', 'Tall Stacks', 'Minimal', 'Corner Nook'],
  decor: ['Plants', 'Art Prints', 'Rugs', 'Cushions', 'Candles', 'Globe', 'Trinkets'],
}

export default function CustomizePage() {
  const [settings, setSettings] = useState(() => { try { return JSON.parse(localStorage.getItem('bookhaus_room')) || defaults } catch { return defaults } })
  const [saved, setSaved] = useState(false)
  function choose(group, value) { setSaved(false); setSettings((current) => group === 'decor' ? { ...current, decor: current.decor.includes(value) ? current.decor.filter((item) => item !== value) : [...current.decor, value] } : { ...current, [group]: value }) }
  function save() { localStorage.setItem('bookhaus_room', JSON.stringify(settings)); setSaved(true) }
  const previewClass = `room-preview theme-${settings.theme.split(' ')[0].toLowerCase()} light-${settings.lighting.split(' ')[0].toLowerCase()}`
  return <><PageHero compact eyebrow="Customize your reading room" title="Make It Feel Like You" description="Create a cozy, personalized space for your books, thoughts, and brighter days." note="Different moods for different chapters of you." /><div className="customize-layout"><section className={previewClass} style={{ backgroundImage: `url(${roomHero})` }}><div className="room-preview__label"><Sparkles /> {settings.theme}</div><p>A cozy space<br />for bolder stories. <span>&hearts;</span></p></section><aside className="cozy-panel customization-panel"><div className="section-heading"><div><p className="eyebrow">Your room, your mood</p><h2>Room Customization</h2></div><Sparkles /></div><ChoiceGroup icon={Sparkles} title="Room Theme" group="theme" choices={options.theme} settings={settings} choose={choose} /><ChoiceGroup icon={Lamp} title="Lighting" group="lighting" choices={options.lighting} settings={settings} choose={choose} /><ChoiceGroup icon={BookOpen} title="Shelf Arrangement" group="shelves" choices={options.shelves} settings={settings} choose={choose} /><ChoiceGroup icon={Leaf} title="Decor" group="decor" choices={options.decor} settings={settings} choose={choose} /><div className="customization-actions"><button type="button" className="btn-secondary" onClick={() => { setSettings(defaults); setSaved(false) }}><RotateCcw /> Reset Room</button><button type="button" className="btn-primary" onClick={save}>{saved ? <Check /> : <Save />} {saved ? 'Room saved locally' : 'Save My Reading Room'}</button></div></aside></div></>
}

function ChoiceGroup({ icon: Icon, title, group, choices, settings, choose }) {
  const selected = settings[group]
  return <section className="choice-group"><h3><Icon /> {title}</h3><div>{choices.map((choice, index) => { const active = Array.isArray(selected) ? selected.includes(choice) : selected === choice; return <button type="button" className={active ? 'is-active' : ''} onClick={() => choose(group, choice)} key={choice}><span className={`choice-swatch choice-swatch--${group}-${index}`} />{choice}{active && <Check />}</button> })}</div></section>
}
