import {useEffect,useState} from 'react';
import {BriefcaseBusiness,Mail,Phone,Search,ShieldCheck,UserRound} from 'lucide-react';
import api from '../api/axiosClient';

export default function PeoplePage({mode}) {
  const [people,setPeople]=useState([]);
  const [query,setQuery]=useState('');
  const [loading,setLoading]=useState(true);
  const isAgents=mode==='agents';
  useEffect(()=>{let mounted=true;setLoading(true);api.get(isAgents?'/agents':'/admin/users').then(response=>{if(mounted)setPeople(response.data);}).finally(()=>{if(mounted)setLoading(false);});return()=>{mounted=false;};},[isAgents]);
  const filtered=people.filter(person=>`${person.displayName||''} ${person.email||''} ${person.role||''}`.toLowerCase().includes(query.toLowerCase()));
  return <section className="people-page">
    <div className="page-heading"><div><p className="eyebrow">{isAgents?'Relationship network':'Workspace directory'}</p><h1>{isAgents?'Agents':'Customers'}</h1><p className="page-description">{isAgents?'Connect customers with the right property specialist.':'Review and manage customer accounts across EstateFlow.'}</p></div><div className="directory-count"><strong>{people.length}</strong><span>{isAgents?'active agents':'accounts'}</span></div></div>
    <div className="directory-toolbar"><div className="directory-search"><Search size={17}/><input value={query} onChange={event=>setQuery(event.target.value)} placeholder={`Search ${isAgents?'agents':'customers'}...`}/></div></div>
    {loading?<div className="directory-empty">Loading directory...</div>:filtered.length===0?<div className="directory-empty">No matching records found.</div>:<div className="people-grid">{filtered.map(person=><article className="person-card" key={person.id}><div className="person-avatar">{isAgents?<BriefcaseBusiness size={21}/>:<UserRound size={21}/>}</div><div className="min-w-0"><h2>{person.displayName||person.email}</h2><p className="person-role">{isAgents?(person.specialization||'Property specialist'):person.role?.toLowerCase()}</p><div className="person-meta"><span><Mail size={14}/>{person.email}</span>{isAgents&&person.phone&&<span><Phone size={14}/>{person.phone}</span>}</div></div><div className="ml-auto">{isAgents?<span className={`availability ${person.available?'available':'unavailable'}`}><span/>{person.available?'Available':'Busy'}</span>:<ShieldCheck className={person.active?'text-emerald-500':'text-slate-400'} size={18}/>}</div></article>)}</div>}
  </section>;
}
