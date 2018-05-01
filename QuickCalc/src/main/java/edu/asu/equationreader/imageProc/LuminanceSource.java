/*
 * Copyright 2009 ZXing authors
 * Copyright 2011 Robert Theis
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
package edu.asu.equationreader.imageProc;


public abstract class LuminanceSource {

  private final int width;
  private final int height;

  protected LuminanceSource(int width, int height) {
    this.width = width;
    this.height = height;
  }


  public abstract byte[] getRow(int y, byte[] row);


  public abstract byte[] getMatrix();

  public final int getWidth() {
    return width;
  }

  public final int getHeight() {
    return height;
  }

  public boolean isCropSupported() {
    return true;
  }


  public LuminanceSource crop(int left, int top, int width, int height) {
    throw new RuntimeException("This luminance source does not support cropping.");
  }


  public boolean isRotateSupported() {
    return false;
  }


  public LuminanceSource rotateCounterClockwise() {
    throw new RuntimeException("This luminance source does not support rotation.");
  }

}
