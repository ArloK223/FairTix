import React from 'react';
import Logo from '../components/Logo';
import { useNavigate } from 'react-router-dom';
function Login() {
   let navigate = useNavigate();
   return (
      <><Logo />
         <h1>LOG IN</h1>
         <label>
         email: <input name="email" type="email"/></label>

         <label>
            password: <input name="password" type="password"></input></label>
         <br /><br />
         <label>
            <input type="submit" value="log in" onClick={() => { navigate("/events"); }}/></label>
      </>
   )
}

export default Login;