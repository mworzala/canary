# Canary Helper

## Changes
 * Shaders
   * position_color - Changed `ColorModulator` to `vec4(1, 1, 1, 0.75)` only when it is `vec4(0, 1, 0, 0.75)`. 
     This allows any color to be used in a debug marker.