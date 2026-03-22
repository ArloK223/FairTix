import React from 'react';
import UseDeviceSize from '../UseDeviceSize';
import logo from '../logo.png';

// don't worry i'll make a better logo later! i have graphic design skills
// i was thinking something white and orange? -skylar
function Logo() {
   const [width] = UseDeviceSize();
   return (
      <div><img width={width} src={logo} alt="really cool logo, trust" /></div>
   )
}

export default Logo;