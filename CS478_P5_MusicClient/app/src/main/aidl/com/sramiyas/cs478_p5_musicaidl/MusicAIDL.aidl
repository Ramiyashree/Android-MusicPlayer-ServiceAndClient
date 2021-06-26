// MusicAIDL.aidl
package com.sramiyas.cs478_p5_musicaidl;

// Declare any non-default types here with import statements


   interface MusicAIDL {
            String getsongURL(int id);
            Bundle getAllInfo();
            Bundle getOneSong(int index);
             }