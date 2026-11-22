import {useState} from 'react';
import {Navigate,Link} from 'react-router-dom';
import {ArrowLeft,Building2,Eye,EyeOff,ShieldCheck,UserRound} from 'lucide-react';
import {useAuth} from '../context/AuthContext';
import {useTheme} from '../context/ThemeContext';

export default function LoginPage(){
  const {user,login,registerCustomer}=useAuth();
  const {theme,toggleTheme}=useTheme();
  const [mode,setMode]=useState('login');
  const [loginRole,setLoginRole]=useState('CUSTOMER');
  const [email,setEmail]=useState('');
  const [password,setPassword]=useState('');
  const [displayName,setDisplayName]=useState('');
  const [phone,setPhone]=useState('');
  const [location,setLocation]=useState('');
  const [budget,setBudget]=useState('');
  const [requirements,setRequirements]=useState('');
  const [showPassword,setShowPassword]=useState(false);
  const [error,setError]=useState('');
  const [busy,setBusy]=useState(false);
  const passwordRules={
    length:password.length>=8,
    uppercase:/[A-Z]/.test(password),
    lowercase:/[a-z]/.test(password),
    number:/\d/.test(password),
    special:/[^A-Za-z0-9]/.test(password)
  };
  const validPassword=Object.values(passwordRules).every(Boolean);

  if(user)return <Navigate to="/dashboard" replace />;

  async function submit(event){
    event.preventDefault();
    setError('');
    setBusy(true);
    try {
      if(mode==='register'){
        if(!validPassword){
          setError('Password does not meet the required rules.');
          setBusy(false);
          return;
        }
        await registerCustomer({email,password,displayName,phone,preferredLocation:location,budgetMax:budget?Number(budget):null,requirements});
        setMode('login');
        setLoginRole('CUSTOMER');
        setPassword('');
        setError('Account created. Sign in as a customer to continue.');
      } else {
        await login(email,password,loginRole);
        window.location.href='/dashboard';
      }
    } catch(err){
      const data=err.response?.data;
      const validationMessage=data?.errors?Object.values(data.errors).join(' '):null;
      const networkMessage=!err.response?'Cannot reach the API. Start the backend on port 8080, then try again.':null;
      setError(validationMessage||data?.message||networkMessage||err.message||'Unable to complete this request.');
    } finally {
      setBusy(false);
    }
  }

  return <div className="login-page">
    <Link to="/" className="login-back"><ArrowLeft size={16}/> Back to EstateFlow</Link>
    <button type="button" onClick={toggleTheme} className="login-theme">{theme==='dark'?'Light mode':'Dark mode'}</button>
    <div className="login-building"/>
    <form onSubmit={submit} className="login-card">
      <div className="login-brand"><img src="/estateflow-mark.svg" alt="" /><div><strong>ESTATEFLOW</strong><span>Property intelligence workspace</span></div></div>
      <div className="login-tabs">
        <button type="button" className={mode==='login'?'active':''} onClick={()=>setMode('login')}>Sign in</button>
        <button type="button" className={mode==='register'?'active':''} onClick={()=>setMode('register')}>Create customer account</button>
      </div>
      {mode==='login'&&<div className="login-role-grid" aria-label="Select account role">
        <button type="button" className={loginRole==='CUSTOMER'?'selected':''} onClick={()=>setLoginRole('CUSTOMER')}>
          <UserRound size={20}/><span><strong>Customer</strong><small>Discover and enquire</small></span>
        </button>
        <button type="button" className={loginRole==='AGENT'?'selected':''} onClick={()=>setLoginRole('AGENT')}>
          <Building2 size={20}/><span><strong>Agent</strong><small>Manage clients and visits</small></span>
        </button>
        <button type="button" className={loginRole==='ADMIN'?'selected':''} onClick={()=>setLoginRole('ADMIN')}>
          <ShieldCheck size={20}/><span><strong>Admin</strong><small>Manage workspace</small></span>
        </button>
      </div>}
      {mode==='register'&&<div className="login-role-grid customer-only">
        <button type="button" className="selected"><ShieldCheck size={20}/><span><strong>Customer</strong><small>Account registration</small></span></button>
      </div>}
      <h1>{mode==='login'?'Welcome back':'Find your next home'}</h1>
      <p className="login-subtitle">{mode==='login'?`Sign in to your ${loginRole.toLowerCase()} workspace.`:'Create a customer account and connect with an agent.'}</p>
      {error&&<p className={`login-error ${error.includes('created')?'success':''}`}>{error}</p>}
      {mode==='register'&&<div className="login-register-grid">
        <input placeholder="Full name" value={displayName} onChange={e=>setDisplayName(e.target.value)} required/>
        <input placeholder="Phone number" value={phone} onChange={e=>setPhone(e.target.value)} required/>
        <input placeholder="Preferred location" value={location} onChange={e=>setLocation(e.target.value)} />
        <input type="number" placeholder="Budget maximum" value={budget} onChange={e=>setBudget(e.target.value)} />
        <textarea className="full" placeholder="What are you looking for?" value={requirements} onChange={e=>setRequirements(e.target.value)} />
      </div>}
      <label>Email</label>
      <input type="email" value={email} onChange={e=>setEmail(e.target.value)} required />
      <label>Password</label>
      <div className="password-field">
        <input type={showPassword?'text':'password'} value={password} onChange={e=>setPassword(e.target.value)} required aria-label="Password" />
        <button type="button" className="password-toggle" onClick={()=>setShowPassword(value=>!value)} aria-label={showPassword?'Hide password':'Show password'} title={showPassword?'Hide password':'Show password'}>
          {showPassword?<EyeOff size={18}/>:<Eye size={18}/>}
        </button>
      </div>
      {mode==='register'&&<div className="password-rules" aria-live="polite">
        <span className={passwordRules.length?'valid':''}>8+ characters</span>
        <span className={passwordRules.uppercase?'valid':''}>Uppercase letter</span>
        <span className={passwordRules.lowercase?'valid':''}>Lowercase letter</span>
        <span className={passwordRules.number?'valid':''}>Number</span>
        <span className={passwordRules.special?'valid':''}>Special character</span>
      </div>}
      <button disabled={busy} className="login-submit">{busy?(mode==='login'?'Signing in...':'Creating account...'):(mode==='login'?`Sign in as ${loginRole.toLowerCase()}`:'Create customer account')}</button>
    </form>
  </div>;
}
