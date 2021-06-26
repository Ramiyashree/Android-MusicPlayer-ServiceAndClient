// MusicAIDL.aidl
package com.sramiyas.cs478_p5_musicaidl;


   interface MusicAIDL {
            String getsongURL(int id);
            Bundle getAllInfo();
            Bundle getOneSong(int index);
             }