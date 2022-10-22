# skript-particle
 Skript addon for creating complex particle effects with minimal hassle.
 
 Skript-Particle is intended to provide syntax for creating complex shapes with particles. Competent and powerful particle syntaxes exist in addons like SkBee, but creating shapes requires a lot more effort and know-how. It's also inherently slower and less performant than Java, even without considering the usual need to re-calculate the shape each time it's drawn.
 
 The goal for Skript-Particle is to provide easy-to-use, flexible, and powerful syntax to create particle shapes. The basic structure is a set of various shapes, like circles, lines, polygons, and more. These shapes are relative to a center point and aren't positioned anywhere specific. The drawing syntax takes a set of shapes, a particle, and a location and draws all the shapes relative to that location. This allows the definition of a bunch of shapes that only need to be defined once and can then be drawn in many places and in many rotations. Since shapes can be rotated as a group, it's also very simple to change the rotation and orientation of your shapes with a single line.
 
## Current Features:
- Shapes:
  - Regular and irregular 2d polygons, with optional height
  - Circles, cylinders, and spheres
  - Arcs and spherical caps
  - Lines, rectangles, and cuboids
  - Helices
- Drawing
  - Three styles: outline, surface, and filled. Some shapes only support one or two of these styles.
  - Rotation to any orientation.
  - Scaling and offsetting.
  - Drawing multiple shapes at once.
  - Combining shapes into a custom shape to be drawn as a single shape.
  - Custom particle data and density per shape
    - Full support for SkBee's particle data syntaxes
    - Gradient colour fields, with custom colour nodes.
  - Debug axes for clear visualization of the orientation of your shape.
  - Shapes are only calculated when they actually change, so you can draw the same shape many times without any performance hit.

 ## Planned Features:
 - Shapes:
   - Ellipsoids
   - Bezier Curves
   - Regular polyhedra
   - Custom shapes defined by a function
   - "Common but difficult to code" shapes like hearts, stars, and more
 - Drawing:
   - Options for who sees the particle (player specific)
   - Custom particle motion (inwards, outwards, clockwise, etc) 
 - Animation:
   - Ability to stretch out drawing a shape over a timespan
   - Ability to continuously draw a shape for a timespan
   - Set up a pre-made animation in a custom structure, play back with one draw call 
 - Better particle syntax:
   - Structure API-based particle definition

 # Syntax
Skript-Particle's syntax is currently in flux and is subject to change. Below are a few small examples of what the syntax looks like currently.
```bash
on load:
    set {local-gradient} to a local particle gradient
    add gradient point from vector(0,2,0) and colour rgb(176,40,0) to points of {local-gradient}
    add gradient point from vector(0,-3,0) and colour rgb(230,160,28) to points of {local-gradient}

 complex shape named "eye":
     shapes:
         set {_pupil} to a spherical cap with radius 3 and cutoff angle 10 degrees
         set particle of {_pupil} to (dust particle using dustOption(black, 1))
         set offset vector of {_pupil} to vector(0,0.4,0)
         set particle density of {_pupil} to 0.1
         add {_pupil} to shapes
        
         set {_iris} to a spherical cap with radius 3 and cutoff angle 25 degrees
         set particle of {_iris} to (dust particle using dustOption(red, 1))
         set offset vector of {_iris} to vector(0,0.2,0)
         set particle density of {_iris} to 0.1
         add {_iris} to shapes

         set {_sclera} to a spherical cap with radius 3 and angle 75 degrees
         set particle of {_sclera} to {local-gradient}
         set particle density of {_sclera} to 0.2
         add {_sclera} to shapes
         
# Draws an eye that watches the player.  
command toggle-eye:
    trigger:
         if {eye} is set:
             broadcast "&c off"
             delete {eye}
             stop
         else:
             broadcast "&a on"
             set {eye} to true
        set {_loc1} to player's location
        set {_eye1} to a copy of complex shape named "eye"
        while {eye} is set:
            set {_v1} to vector from {_loc1} to player's head location
            set normal of {_eye1} to {_v1}
            draw {_eye1} at {_loc1}
            wait 1 ticks
```

```bash
complex shape named "magic-circle":
    particle: electric spark
    shapes:
        set {_circle} to a circle of radius 1.75
        add {_circle} to shapes

        set {_circle} to a circle of radius 4
        add {_circle} to shapes

        set {_circle} to a circle of radius 4.45
        add {_circle} to shapes
        
        set {_v1} to spherical vector with radius 4, yaw 0, pitch 0
        set {_v2} to spherical vector with radius 4, yaw 120, pitch 0
        set {_line} to line between {_v1} and {_v2}
        
        loop 6 times:
            rotate shape {_line} around y-axis by 60 degrees 
            add a copy of {_line} to shapes

```
 
 ![2022-08-28_00 10 57](https://user-images.githubusercontent.com/10354869/187062233-5f51ba7b-60f4-44f8-bf6b-862a4e2381fd.png)


https://user-images.githubusercontent.com/10354869/187062241-d3c51f86-4129-4f8b-9ce3-2d0037779e4e.mp4


