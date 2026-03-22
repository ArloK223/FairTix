import React from 'react';
import UseDeviceSize from '../UseDeviceSize';
import logo from '../logo.png';

function Logo() {
   const [width] = UseDeviceSize();
   return (
      <div><img width={width} src={logo} alt="really cool logo, trust" /></div>
   )
}

export default Logo;