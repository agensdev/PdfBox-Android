/*
 * Copyright 2018 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tom_roush.pdfbox.pdmodel.interactive.annotation.handlers;

import android.util.Log;

import java.io.IOException;

import com.tom_roush.pdfbox.io.IOUtils;
import com.tom_roush.pdfbox.pdmodel.PDAppearanceContentStream;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDColor;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;

/**
 * Handler to generate the ink annotations appearance.
 *
 */
public class PDInkAppearanceHandler extends PDAbstractAppearanceHandler
{
   public PDInkAppearanceHandler(PDAnnotation annotation)
   {
      super(annotation);
   }

   public PDInkAppearanceHandler(PDAnnotation annotation, PDDocument document)
   {
      super(annotation, document);
   }

   @Override
   public void generateAppearanceStreams()
   {
      generateNormalAppearance();
      generateRolloverAppearance();
      generateDownAppearance();
   }

   @Override
   public void generateNormalAppearance()
   {
      PDAnnotationMarkup ink = (PDAnnotationMarkup) getAnnotation();
      // PDF spec does not mention /Border for ink annotations, but it is used if /BS is not available
      AnnotationBorder ab = AnnotationBorder.getAnnotationBorder(ink, ink.getBorderStyle());
      PDColor color = ink.getColor();
      if (color == null || color.getComponents().length == 0 || Float.compare(ab.width, 0) == 0)
      {
         return;
      }

      PDAppearanceContentStream cs = null;

      try
      {
         cs = getNormalAppearanceAsContentStream();

         setOpacity(cs, ink.getConstantOpacity());

         cs.setStrokingColor(color);
         if (ab.dashArray != null)
         {
            cs.setLineDashPattern(ab.dashArray, 0);
         }
         cs.setLineWidth(ab.width);

         for (float[] pathArray : ink.getInkList())
         {
            int nPoints = pathArray.length / 2;

            // "When drawn, the points shall be connected by straight lines or curves 
            // in an implementation-dependent way" - we do lines.
            for (int i = 0; i < nPoints; ++i)
            {
               float x = pathArray[i * 2];
               float y = pathArray[i * 2 + 1];

               if (i == 0)
               {
                  cs.moveTo(x, y);
               }
               else
               {
                  cs.lineTo(x, y);
               }
            }
            cs.stroke();
         }
      }
      catch (IOException ex)
      {
         Log.e("PdfBox-Android", ex.getMessage(), ex);
      }
      finally
      {
         IOUtils.closeQuietly(cs);
      }
   }

   @Override
   public void generateRolloverAppearance()
   {
      // No rollover appearance generated
   }

   @Override
   public void generateDownAppearance()
   {
      // No down appearance generated
   }
}