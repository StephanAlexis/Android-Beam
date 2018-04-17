package com.brett.beam.models;

import android.graphics.Bitmap;

/**
 * Created by Stephan on 4/15/2018.
 */

public class Messsage {
        private String expediteur ;
        private String message;
        private String laDate;
        private boolean isImage;
        private Bitmap bitmap;

        public Messsage(String expediteur, String message, String laDate) {
            this.expediteur = expediteur;
            this.message = message;
            this.laDate = laDate;
        }
        public Messsage(String expediteur, Bitmap bitmap, String laDate)
        {
            this.expediteur = expediteur;
            this.bitmap = bitmap;
            this.laDate = laDate;
            this.isImage=true;
        }

        public String getExpediteur() {
            return expediteur;
        }

        public void setExpediteur(String expediteur) {
            this.expediteur = expediteur;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getLaDate() {
            return laDate;
        }

        public void setLaDate(String laDate) {
            this.laDate = laDate;
        }

         public void setBitmap(Bitmap bitmap)
         {
             this.bitmap=bitmap;
         }
         public Bitmap getBitmap()
         {
             return bitmap;
         }

         public boolean getIsImage()
         {
             return isImage;
         }
}
