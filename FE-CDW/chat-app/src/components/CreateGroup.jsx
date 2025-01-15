import React, { useState } from 'react'
import { BsArrowLeft, BsCheck2, BsPencil } from 'react-icons/bs'

export default function CreateGroup({handleNavigate}) {
  const[newGroup, setNewGroup] = useState(false);

  return (
    <div className=' w-full h-full'>
           <div className='pb-3 pl-3 pt-20 flex items-center space-x-4 bg-[#008069] text-white'>
               <BsArrowLeft className='cursor-pointer text-2xl font-bold' onClick={() =>handleNavigate(false)}/>
               <p className='cursor-pointer font-semibold text-xl '>Tạo nhóm chat</p>
           </div>
           
       </div>
  )
}
