import {useEffect,useMemo,useState} from 'react';
import {Archive, Bath, BedDouble, Building2, Heart, MapPin, Maximize2, Pencil, Search, SlidersHorizontal, Sparkles, X} from 'lucide-react';
import api from '../api/axiosClient';
import StatusBadge from '../components/common/StatusBadge';
import Modal from '../components/common/Modal';
import {useAuth} from '../context/AuthContext';

const typeImages={
  Apartment:'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=900&q=80',
  Villa:'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=900&q=80',
  Office:'https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=900&q=80',
  default:'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=900&q=80'
};
const emptyForm={title:'',type:'Apartment',price:'',basePrice:'',areaSqft:'',location:'',configuration:'',imageUrl:''};
const uuidPattern=/^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
const demoProperties=[
 {id:'demo-skyline',title:'Skyline Heights',type:'Apartment',status:'AVAILABLE',price:12500000,areaSqft:1850,location:'Marathahalli, Bengaluru',configuration:'3 BHK',imageUrl:typeImages.Apartment},
 {id:'demo-palm',title:'Palm Grove Villa',type:'Villa',status:'AVAILABLE',price:28500000,areaSqft:3200,location:'Whitefield, Bengaluru',configuration:'4 BHK',imageUrl:typeImages.Villa},
 {id:'demo-nest',title:'Urban Nest',type:'Apartment',status:'AVAILABLE',price:8750000,areaSqft:1250,location:'Indiranagar, Bengaluru',configuration:'2 BHK',imageUrl:typeImages.Apartment},
 {id:'demo-trade',title:'Prime Trade Centre',type:'Commercial space',status:'AVAILABLE',price:19000000,areaSqft:2400,location:'MG Road, Bengaluru',configuration:'Office floor',imageUrl:typeImages.Office}
];

function PropertyCard({property,favorite,onFavorite,onOpen,canManage,onEdit,onArchive}) {
  return <article className="motion-card group overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm transition hover:-translate-y-2 hover:shadow-xl">
    <div className="relative h-48 overflow-hidden bg-slate-200">
      <img src={property.imageUrl||typeImages[property.type]||typeImages.default} alt={property.title} className="h-full w-full object-cover transition duration-500 group-hover:scale-105"/>
      <div className="absolute inset-x-0 top-0 flex items-center justify-between p-4">
        <StatusBadge status={property.status}/>
        <div className="flex items-center gap-2">
          {canManage&&<><button aria-label={`Edit ${property.title}`} title="Edit property" onClick={()=>onEdit(property)} className="rounded-full bg-white/90 p-2 text-slate-700 hover:text-blue-700"><Pencil size={16}/></button><button aria-label={`Remove ${property.title}`} title="Remove property" onClick={()=>onArchive(property)} className="rounded-full bg-white/90 p-2 text-slate-700 hover:text-rose-600"><Archive size={16}/></button></>}
          <button aria-label={favorite?'Remove from saved homes':'Save home'} onClick={()=>onFavorite(property.id)} className={`rounded-full p-2 backdrop-blur ${favorite?'bg-rose-500 text-white':'bg-white/90 text-slate-600 hover:text-rose-500'}`}><Heart size={17} fill={favorite?'currentColor':'none'}/></button>
        </div>
      </div>
      <div className="absolute bottom-3 left-3 rounded-full bg-slate-950/75 px-3 py-1 text-xs font-medium text-white"><Sparkles size={12} className="mr-1 inline"/> Featured listing</div>
    </div>
    <button onClick={()=>onOpen(property)} className="block w-full text-left">
      <div className="p-5">
        <div className="flex items-start justify-between gap-3"><div><p className="text-xs font-bold uppercase tracking-widest text-blue-600">{property.type}</p><h3 className="mt-1 text-lg font-bold text-slate-900">{property.title}</h3></div><Maximize2 size={17} className="mt-1 text-slate-400"/></div>
        <p className="mt-3 flex items-center gap-1 text-sm text-slate-500"><MapPin size={14}/> {property.location||'Prime location · Bengaluru'}</p>
        <div className="mt-5 flex items-center gap-4 border-t border-slate-100 pt-4 text-sm text-slate-500"><span><BedDouble size={15} className="mr-1 inline"/>2 beds</span><span><Bath size={15} className="mr-1 inline"/>2 baths</span><span><Maximize2 size={15} className="mr-1 inline"/>{property.areaSqft} sq.ft.</span></div>
        <p className="mt-4 text-xl font-black text-slate-950">₹{Number(property.price).toLocaleString('en-IN')}</p>
      </div>
    </button>
  </article>;
}

export default function PropertiesPage(){
  const {user}=useAuth();
  const [rows,setRows]=useState([]),[open,setOpen]=useState(false),[selected,setSelected]=useState(null),[editingProperty,setEditingProperty]=useState(null),[form,setForm]=useState(emptyForm),[query,setQuery]=useState(''),[type,setType]=useState('ALL'),[sort,setSort]=useState('featured'),[favorites,setFavorites]=useState(()=>JSON.parse(localStorage.getItem('crm_favorites')||'[]')),[error,setError]=useState('');
  const canManage=['SUPER_ADMIN','OWNER'].includes(user?.role);
  async function load(){
    const localRows=JSON.parse(localStorage.getItem('estateflow_properties')||'[]');
    try{
      const response=await api.get('/properties');
      const byTitle=new Map();
      [...demoProperties,...localRows,...response.data].forEach(row=>byTitle.set(row.title.toLowerCase(),row));
      setRows([...byTitle.values()].filter(row=>row.status!=='ARCHIVED'));
      setError('');
    }catch(err){
      if(err.response?.status===401){setRows([]);setError('Your session expired. Please sign in again.');return;}
      if(err.response?.status===403){setRows([]);setError('You do not have permission to view properties. Please sign in with an authorized account.');return;}
      setRows([...demoProperties,...localRows]);
      setError(err.response
        ?`Could not load properties (HTTP ${err.response.status}). Showing your saved local listings.`
        :'Backend connection unavailable. Showing your saved local listings.');
    }
  }
  useEffect(()=>{load();},[]);
  function toggleFavorite(id){const next=favorites.includes(id)?favorites.filter(x=>x!==id):[...favorites,id];setFavorites(next);localStorage.setItem('crm_favorites',JSON.stringify(next));}
  function startCreate(){setEditingProperty(null);setForm(emptyForm);setError('');setOpen(true);}
  function startEdit(property){
    setEditingProperty(property);
    setForm({title:property.title,type:property.type,price:String(property.price??''),basePrice:property.basePrice==null?'':String(property.basePrice),areaSqft:String(property.areaSqft??''),location:property.location||'',configuration:property.configuration||'',imageUrl:property.imageUrl||'',status:property.status||'AVAILABLE'});
    setSelected(null);
    setError('');
    setOpen(true);
  }
  const types=useMemo(()=>['ALL',...new Set(rows.map(p=>p.type))],[rows]);
  const filtered=useMemo(()=>rows.filter(p=>(type==='ALL'||p.type===type)&&`${p.title} ${p.type}`.toLowerCase().includes(query.toLowerCase())).sort((a,b)=>sort==='price-low'?a.price-b.price:sort==='price-high'?b.price-a.price:0),[rows,type,query,sort]);
  async function save(e){
    e.preventDefault();
    setError('');
    const payload={title:form.title,type:form.type,price:Number(form.price),basePrice:form.basePrice?Number(form.basePrice):null,areaSqft:Number(form.areaSqft),location:form.location||'Bengaluru',configuration:form.configuration||'Property listing',imageUrl:form.imageUrl||(editingProperty?null:typeImages[form.type]||typeImages.default)};
    if(editingProperty){
      let updated={...editingProperty,...payload,status:form.status};
      if(uuidPattern.test(String(editingProperty.id))){
        try{
          const response=await api.put(`/properties/${editingProperty.id}`,{...payload,status:form.status});
          updated=response.data;
        }catch(err){
          setError(err.response?.data?.message||'Could not update property.');
          return;
        }
      }else{
        const localRows=JSON.parse(localStorage.getItem('estateflow_properties')||'[]');
        if(!String(editingProperty.id).startsWith('local-')){setError('Demo properties cannot be edited. Connect to the backend to manage live listings.');return;}
        localStorage.setItem('estateflow_properties',JSON.stringify(localRows.map(row=>row.id===editingProperty.id?updated:row)));
      }
      setOpen(false);
      setEditingProperty(null);
      setForm(emptyForm);
      await load();
      return;
    }
    let saved=payload;
    try{
      const response=await api.post('/properties',payload);
      saved={...payload,...response.data};
    }catch(err){
      if(err.response?.status===401){setError('Sign in again before adding property.');return;}
      saved={...payload,id:`local-${Date.now()}`,status:'AVAILABLE'};
    }
    const localRows=JSON.parse(localStorage.getItem('estateflow_properties')||'[]');
    const next=[...localRows.filter(row=>row.title.toLowerCase()!=saved.title.toLowerCase()),saved];
    localStorage.setItem('estateflow_properties',JSON.stringify(next));
    setOpen(false);
    setForm(emptyForm);
    await load();
  }
  async function archive(property){
    if(!window.confirm(`Remove "${property.title}" from active listings? Its related history will be preserved.`))return;
    setError('');
    if(uuidPattern.test(String(property.id))){
      try{await api.delete(`/properties/${property.id}`);}
      catch(err){setError(err.response?.data?.message||'Could not remove property.');return;}
    }else{
      const localRows=JSON.parse(localStorage.getItem('estateflow_properties')||'[]');
      if(!String(property.id).startsWith('local-')){setError('Demo properties cannot be removed. Connect to the backend to manage live listings.');return;}
      localStorage.setItem('estateflow_properties',JSON.stringify(localRows.filter(row=>row.id!==property.id)));
    }
    if(selected?.id===property.id)setSelected(null);
    await load();
  }
  return <div>
    <section className="marketplace-hero relative mb-8 overflow-hidden rounded-3xl px-6 py-8 md:px-10 md:py-10">
      <div className="absolute -right-10 -top-20 h-64 w-64 rounded-full bg-blue-500/30 blur-3xl"/>
      <div className="relative max-w-2xl"><p className="mb-3 text-xs font-bold uppercase tracking-[0.25em] text-blue-300">Curated property collection</p><h1 className="text-3xl font-black tracking-tight md:text-4xl">{user?.role==='CUSTOMER'?'Find a place that feels like home.':'Your property marketplace'}</h1><p className="hero-copy mt-3 max-w-xl text-sm leading-6 text-slate-300">Explore verified spaces with the details you need to make your next move confidently.</p><div className="marketplace-search mt-6 flex max-w-xl items-center gap-3 rounded-2xl p-2 shadow-2xl"><Search className="ml-2 text-slate-400" size={19}/><input aria-label="Search properties" className="border-0 !p-2 !ring-0" placeholder="Search by property name or type..." value={query} onChange={e=>setQuery(e.target.value)}/></div></div>
    </section>
    <div className="mb-6 flex flex-wrap items-center justify-between gap-3"><div><h2 className="text-xl font-bold">Explore listings</h2><p className="text-sm text-slate-500">{filtered.length} homes matching your search {favorites.length>0&&`· ${favorites.length} saved`}</p></div><div className="flex flex-wrap items-center gap-2"><div className="flex items-center gap-2 rounded-xl border bg-white px-3 py-2 text-sm"><SlidersHorizontal size={15}/><select className="w-auto border-0 p-0 text-sm focus:ring-0" value={type} onChange={e=>setType(e.target.value)}>{types.map(t=><option key={t} value={t}>{t==='ALL'?'All types':t}</option>)}</select></div><select className="w-auto rounded-xl border bg-white px-3 py-2 text-sm" value={sort} onChange={e=>setSort(e.target.value)}><option value="featured">Featured first</option><option value="price-low">Price: low to high</option><option value="price-high">Price: high to low</option></select>{canManage&&<button onClick={startCreate} className="rounded-xl bg-blue-700 px-4 py-2 text-sm font-semibold text-white shadow-lg shadow-blue-700/20">Add property</button>}</div></div>
    {error&&<p className="mb-4 text-sm text-rose-600">{error}</p>}
    <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">{filtered.map(p=><PropertyCard key={p.id} property={p} favorite={favorites.includes(p.id)} onFavorite={toggleFavorite} onOpen={setSelected} canManage={canManage&&(uuidPattern.test(String(p.id))||String(p.id).startsWith('local-'))} onEdit={startEdit} onArchive={archive}/>)}</div>
    {!filtered.length&&!error&&<div className="rounded-3xl border border-dashed bg-white p-12 text-center"><Building2 className="mx-auto text-slate-300" size={34}/><p className="mt-3 font-semibold">No properties match those filters</p><p className="mt-1 text-sm text-slate-500">Try another search or clear the filters.</p></div>}
    <Modal title={editingProperty?'Edit property':'Create property'} open={open} onClose={()=>{setOpen(false);setEditingProperty(null);}}>
      <form onSubmit={save} className="space-y-3">
        <input placeholder="Property title" value={form.title} onChange={e=>setForm({...form,title:e.target.value})} required/>
        <input placeholder="Type (Apartment, Villa...)" value={form.type} onChange={e=>setForm({...form,type:e.target.value})} required/>
        <input type="number" min="1" placeholder="Listing price" value={form.price} onChange={e=>setForm({...form,price:e.target.value})} required/>
        <input type="number" min="1" placeholder="Base price" value={form.basePrice} onChange={e=>setForm({...form,basePrice:e.target.value})} required/>
        <input type="number" min="1" placeholder="Area in sq.ft." value={form.areaSqft} onChange={e=>setForm({...form,areaSqft:e.target.value})} required/>
        <input placeholder="Location" value={form.location} onChange={e=>setForm({...form,location:e.target.value})}/>
        <input placeholder="Configuration" value={form.configuration} onChange={e=>setForm({...form,configuration:e.target.value})}/>
        {editingProperty&&<select aria-label="Property status" value={form.status} onChange={e=>setForm({...form,status:e.target.value})}><option value="AVAILABLE">Available</option><option value="RESERVED">Reserved</option><option value="SOLD">Sold</option></select>}
        <label className="block text-sm font-semibold text-slate-700">Property / site photo
          <input className="mt-2 block w-full" type="file" accept="image/jpeg,image/png,image/webp" onChange={e=>{
            const file=e.target.files?.[0];
            if(!file)return;
            if(file.size>2*1024*1024){setError('Photo must be smaller than 2 MB.');e.target.value='';return;}
            const reader=new FileReader();
            reader.onload=()=>setForm(current=>({...current,imageUrl:String(reader.result)}));
            reader.onerror=()=>setError('Could not read the selected photo. Please try another image.');
            reader.readAsDataURL(file);
          }}/>
        </label>
        {form.imageUrl&&<div className="flex items-center justify-between gap-3 rounded-lg border p-2"><img src={form.imageUrl} alt="Selected property" className="h-16 w-24 rounded object-cover"/><button type="button" onClick={()=>setForm({...form,imageUrl:''})} className="text-sm font-semibold text-rose-600">Remove photo</button></div>}
        <button className="w-full rounded-lg bg-blue-700 py-2 text-white">{editingProperty?'Save changes':'Save property'}</button>
      </form>
    </Modal>
    {selected&&<div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 p-4" onClick={()=>setSelected(null)}><div className="max-h-[90vh] w-full max-w-lg overflow-hidden rounded-3xl bg-white shadow-2xl" onClick={e=>e.stopPropagation()}><div className="relative h-56"><img src={selected.imageUrl||typeImages[selected.type]||typeImages.default} alt={selected.title} className="h-full w-full object-cover"/><button onClick={()=>setSelected(null)} className="absolute right-4 top-4 rounded-full bg-white/90 p-2"><X size={18}/></button></div><div className="p-6"><div className="flex items-start justify-between"><div><p className="text-xs font-bold uppercase tracking-widest text-blue-600">{selected.type}</p><h2 className="mt-1 text-2xl font-black">{selected.title}</h2></div><StatusBadge status={selected.status}/></div><p className="mt-2 flex items-center gap-1 text-sm text-slate-500"><MapPin size={14}/>{selected.location||'Bengaluru'}</p><p className="mt-5 text-3xl font-black">₹{Number(selected.price).toLocaleString('en-IN')}</p><div className="mt-5 grid grid-cols-3 gap-3 rounded-2xl bg-slate-50 p-4 text-center text-sm text-slate-600"><span><BedDouble className="mx-auto mb-1 text-blue-600" size={18}/>2 beds</span><span><Bath className="mx-auto mb-1 text-blue-600" size={18}/>2 baths</span><span><Maximize2 className="mx-auto mb-1 text-blue-600" size={18}/>{selected.areaSqft} sqft</span></div><button onClick={()=>toggleFavorite(selected.id)} className="mt-5 w-full rounded-xl bg-slate-950 py-3 font-semibold text-white">{favorites.includes(selected.id)?'Remove from saved homes':'Save this home'}</button>{canManage&&(uuidPattern.test(String(selected.id))||String(selected.id).startsWith('local-'))&&<div className="mt-3 flex gap-2"><button onClick={()=>startEdit(selected)} className="flex-1 rounded-xl border py-3 font-semibold"><Pencil size={15} className="mr-2 inline"/>Edit property</button><button onClick={()=>archive(selected)} className="flex-1 rounded-xl border border-rose-200 py-3 font-semibold text-rose-600"><Archive size={15} className="mr-2 inline"/>Remove property</button></div>}</div></div></div>}
  </div>;
}
