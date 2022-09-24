# skript-particle
 Skript addon for creating complex particle effects with minimal hassle.
 
 Skript-Particle is intended to provide syntax for creating complex shapes with particles. Competent and powerful particle syntaxes exist in addons like SkBee, but creating shapes requires a lot more effort and know-how. It's also inherently slower and less performant than Java, even without considering the usual need to re-calculate the shape each time it's drawn.
 
 The goal for Skript-Particle is to provide easy-to-use, flexible, and powerful syntax to create particle shapes. The basic structure is a set of various shapes, like circles, lines, polygons, and more. These shapes are relative to a center point and aren't positioned anywhere specific. The drawing syntax takes a set of shapes, a particle, and a location and draws all the shapes relative to that location. This allows the definition of a bunch of shapes that only need to be defined once and can then be drawn in many places and in many rotations. Since shapes can be rotated as a group, it's also very simple to change the rotation and orientation of your shapes with a single line.
 
 ## Goals:
 - Shapes:
   - Regular polygons
   - Arcs, sphere sections
   - Spheres (fibonacci and basic)
   - Ellipsoids
   - Arbitrary polygons from point list
   - Cuboids
   - Regular Polyhedra
   - Bezier Curves
   - Spirals
 - Drawing:
   - Fill in shapes
   - Options for who sees the particle (player specific)
 - Animation
   - Ability to stretch out drawing a shape over a timespan
   - Ability to continuously draw a shape for a timespan
   - Set up a pre-made animation in a custom structure, play back with one draw call 
 
 # Syntax
 Currently, there are only a few syntaxes in Skript-Particle. Circles, Lines, rotation expressions, and the draw effect.

 You can also combine shapes in a complex shape structure to create new shapes.
 
 **THIS IS ALL CURRENTLY OUT OF DATE**
 
 ## Shapes
 
 ### Circle
 ```
 [(the|a)] circle [(with|of)] radius %number% [(and|[and] with) [a] step size [of] %-number% [(degrees|radians)]]
 [(the|a)] circle [(with|of)] radius %number% (and|[and] with) [a] particle count [of] %number%
 [(the|a)] circle [(with|of)] radius %number% (and|[and] with) %number% (points|particles)
 ```
 Circles only have a radius and a stepsize/particle count. Step sizes are in degrees by default. If a step size is omitted, the default value will be 12 degrees or 30 particles.
 
 #### Examples
 ```tcl
 the circle with radius 10 
 circle with radius 1 with step size 10 degrees
 a circle of radius 3.25 with 100 particles
 circle radius 3 with step size (3.141 / 2) radians
 ```
 
 ### Line
 ```
 [(the|a)] line (from|between) %vector% (to|and) %vector% [with [a] step size [of] %-number% [meters]]
 [(the|a)] line (from|between) %location% (to|and) %location% [with [a] step size [of] %-number% [meters]]
 [(the|a)] line (in [the]|from) direction %vector% (and|[and] with) length %number% [with [a] step size [of] %-number% [meters]]
 ```
 Lines have a start and end point and a step size, but they're more flexible than most shapes. Since lines are typically drawn from a location to another, rather than centered at some location like more other shapes, they can be defined in a few ways. The first and third syntaxes create relative lines, the first being between two points defined by vectors from a center point, and the second being from the center point in some direction for some length.
 
 The second syntax, though, is from one location to another. Lines defined by this syntax can be treated like a line from the center point to a vector, which would be the vector between the two locations, but they can also be drawn without a center point. In that case the line will simply be drawn between the two locations, with no fuss.
 
 #### Examples
 ```tcl
 line from vector(1,0,0) to vector(0,0,1)
 a line from player's head to player's target block with step size 0.5
 the line in the direction (vector from yaw player's yaw, pitch player's pitch) with length 10
 ```

## Complex Shapes
Complex shapes leverage the new Structure API to allow you to easily create new shapes using existing shapes. You can choose a particle, a normal, a rotation, all the normal stuff a shape can have. However, you can also add multiple shapes to the complex shape, which will all be drawn relative to however the complex shape is drawn. This makes defining particles effects and using them later even easier.

### Structure
```bash
[a] [new] complex shape [named|with [the] name|with [the] id] %string%:
    particle:    particle # optional
    normal:      vector   # optional
    orientation: number   # optional
    shapes:               # required
        add %shapes% to [the] [list of] shapes
```

 
 ## Rotation
 Shapes have two attributes that determine their rotation. A normal vector, which by default points straight up, and an orientation value, which allows rotation about the normal vector. This is subject to change, though, as a yaw/pitch + rotation system may be more intuitive for users.
 
 Shapes can be created with normal vectors and orientation, though this can cause very long lines and is discouraged. The two property syntaxes are as follows:
 
 ### Normal Vector
 ```
 normal [vector] of %shapes%
 %shapes%'[s] normal [vector]
 ```
 These vectors can be set, deleted, manipulated, and rotated like any other vector, and these changes will affect the rotation of the shape. 
 
 ### Orientation
 ```
 orientation of %shapes%
 %shapes%'[s] orientation
 ```
 Orientation is a degree value that corresponding to rotation around the normal vector. I'm considering changing the name but "rotation" is too broad and "yaw" conflicts with the normal idea of yaw. Suggestions welcome.
 
 ### Copying Shapes with New Rotations
 Additionally, one can create a copy of a shape with a new normal vector and/or orientation value with the following syntax. The original shape will not be affected.
 ```
 %shapes% with normal [vector] %vector%
 %shapes% (with orientation [at]|oriented [at|with]) %number% [(degrees|radians)]
 ```
 
 ## Drawing
 Drawing is pretty simple. Select your shapes, the particle to draw with, and possibly the location to draw at. Currently, only basic SkBee particles are valid and they will be drawn with 0 speed or extra data. However, the plan is to incorporate all extra data so particles can be draw with uniform speeds, varying speeds, offsets, colours, and more.
 ```
 draw %shapes% (with|using) %particle% [centered] at %location%
 draw %lines% (with|using) %particle%
 ```
 
 #### Examples
 ```
 draw (a circle with radius 5) using flame centered at player
 draw (line from player's head to player's target block) using flame
 ```
 
 # Example Code
 Here's a quick example of a "magic circle" particle effect that's easily rotated and moved around:
 ```tcl
complex shape named "magic-circle":
    particle: electric spark
    shapes:
        add (a circle with radius 1.75 with 36 particles) to shapes
        add (a circle with radius 4 with 100 particles) to shapes
        add (a circle with radius 4.45 with 120 particles) to shapes
        
        set {_v1} to spherical vector with radius 4, yaw 0, pitch 0
        set {_v2} to spherical vector with radius 4, yaw 120, pitch 0
        add (a line from {_v1} to {_v2}) to shapes
        add (a line from {_v1} to {_v2} oriented at 60 degrees) to shapes
        add (a line from {_v1} to {_v2} oriented at 120 degrees) to shapes
        add (a line from {_v1} to {_v2} oriented at 180 degrees) to shapes
        add (a line from {_v1} to {_v2} oriented at 240 degrees) to shapes
        add (a line from {_v1} to {_v2} oriented at 300 degrees) to shapes

command magic-circle-1:
    trigger:
        draw complex shape named "magic-circle" at player using flame
        
command magic-circle-2:
    trigger:
        loop 120 times:
            # this doesn't change the base shape rotation, as it creates a copy of the base shape
            draw (complex shape named "magic-circle" oriented at loop-number degrees) at player
            wait 1 tick
            
command magic-circle-3:
    trigger:
        set {_mc} to complex shape named "magic-circle"
        loop 180 times:
            # these actually change the base shape's rotation, so be careful
            add 3 to yaw of (normal vector of {_mc})
            add 1 to pitch of (normal vector of {_mc})
            draw {_mc} at player
            wait 1 tick
        reset (normal vector of {_mc})
 ```
 
 ![2022-08-28_00 10 57](https://user-images.githubusercontent.com/10354869/187062233-5f51ba7b-60f4-44f8-bf6b-862a4e2381fd.png)


https://user-images.githubusercontent.com/10354869/187062241-d3c51f86-4129-4f8b-9ce3-2d0037779e4e.mp4


