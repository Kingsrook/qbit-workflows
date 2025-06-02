/*
 * Copyright © 2022-2023. ColdTrack <contact@coldtrack.com>.  All Rights Reserved.
 */

package com.kingsrook.qbits.workflows.tools;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.io.FileUtils;


/*******************************************************************************
 ** make a base64 url out of an SVG material UI icon, for use in a workflow step
 ** type definition.
 *******************************************************************************/
public class MakeBase64MUIIcon
{
   private String iconName = null;
   private String dir = null;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void main(String[] args)
   {
      MakeBase64MUIIcon makeBase64MUIIcon = new MakeBase64MUIIcon();
      makeBase64MUIIcon.dir = "/Users/dkelkhoff/git/kingsrook/qqq-frontend-material-dashboard/node_modules/@mui/icons-material/";
      makeBase64MUIIcon.iconName = "TextDecrease";
      makeBase64MUIIcon.run();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Integer run()
   {
      try
      {
         String js = FileUtils.readFileToString(new File(dir + iconName + ".js"), StandardCharsets.UTF_8);
         if(!StringUtils.hasContent(js))
         {
            throw (new Exception("No javascript found for icon: " + iconName));
         }

         String path = null;
         for(String line : js.split("\n"))
         {
            if(line.contains(" d: "))
            {
               path = line.split("\"", 3)[1];
            }
         }
         // System.out.println(path);

         String svg = """
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
               <path d="%s"/>
            </svg>
            """.formatted(path);
         // System.out.println(svg);

         String base64 = Base64.getEncoder().encodeToString(svg.getBytes());
         // System.out.println(base64);

         String imageUrl = "data:image/svg+xml;base64," + base64;

         System.out.println(imageUrl);
      }
      catch(Exception e)
      {
         e.printStackTrace();
         return (1);
      }

      return (0);
   }

}
