import React from 'react';
import Logo from '../components/Logo';
import { useNavigate } from 'react-router-dom';
function Signup() {
   let navigate = useNavigate();
   return (
      <>
      <Logo/>
         <h1>SIGN UP</h1>
         <label>
            email: <input name="email" type="email" /></label>
         <label>
            phone number: <input name="phone number" type="tel" /></label>
         <label>
            birthday <input name="birthday" type="date" /></label>
         <label>
            password: <input name="password" type="password"></input></label>
         <label>
            confirm password: <input name="confirmpassword" type="password"></input></label>
         <label>
            <input type="submit" value="sign-up" onClick={() => { navigate("/login"); }} /></label>


         { // figure out how to implement an age check, and also make sure the passwords match. look into php?
         }
      </>
   )
}

export default Signup;