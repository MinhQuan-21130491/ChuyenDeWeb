import React from 'react'

export default function Status() {
  return (
    <div className='w-screen h-screen bg-slate-500'>
        <div className='w-full py-14 '>
            <div className='flex bg-[#f0f2f5] h-[80vh] absolute top-16 lg:left-40 lg:right-40 xl:left-60 xl:right-60 sm:left-10 sm:right-10 z-50 shadow-md'>
                {/* left side part */}
                <div className='left md:w-[250px] sm:w-[100px] bg-[#1e262c] h-full flex flex-col'>
                    <div>

                    </div>
                </div>
                {/* right side part */}
                <div  className='right flex-1'>
                    
                </div>
            </div>
        </div>
    </div>
  )
}
