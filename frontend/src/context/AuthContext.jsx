import {createContext,useContext,useMemo,useState} from 'react';
import api from '../api/axiosClient';
const AuthContext=createContext(null);
export function AuthProvider({children}) {
  const [user,setUser]=useState(()=>JSON.parse(localStorage.getItem('crm_user')||'null'));
  async function login(email,password) {
    const {data}=await api.post('/auth/login',{email,password});
    localStorage.setItem('crm_token',data.token);
    const u={id:data.userId,email:data.email,role:data.role};localStorage.setItem('crm_user',JSON.stringify(u));setUser(u);return u;
  }
  async function registerCustomer(payload) {
    const {data}=await api.post('/auth/register/customer',payload);
    return data;
  }
  function logout(){localStorage.removeItem('crm_token');localStorage.removeItem('crm_user');setUser(null);window.location.href='/';}
  const value=useMemo(()=>({user,login,registerCustomer,logout}),[user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export const useAuth=()=>useContext(AuthContext);
