# skript-particle
 Skript addon for creating complex particle effects with minimal hassle.
 
 Skript-Particle is intended to provide syntax for creating complex shapes with particles. Competent and powerful single particle syntax exists in addons like SkBee, but to create shapes one must leverage vectors, loops, functions, and other complicated vanilla syntax. It's possible, but it takes a significant effort and knowledge in how to draw 3D shapes. This method is also inherently slower and less performant than Java, even without considering the typical need to re-calculate the shape each time it's drawn in a new place.
 
 The goal for Skript-Particle is to provide easy-to-use, flexible, and powerful syntax to create particle shapes. The basic structure is a set of syntaxes that generate various shapes, like circles, lines, polygons, and more. These shapes generally are relative and aren't positioned anywhere until they are drawn. The drawing syntax takes a set of shapes, a particle, and a location and draws all the shapes relative to that location. This allows the definition of complex series of shapes that can be defined once and then drawn in many places with ease. Since shapes can be rotated as a group, it's also very simple to change the rotation and orientation of your complex series with a single line.
 
 ## Goals:
 - Shapes:
   - Regular polygons
   - Spheres (fibonacci and basic)
   - Ellipsoids
   - Arbitrary polygons from point list
   - Cuboids
   - Regular Polyhedra
   - Custom Shapes (use new structure API)
   - Bezier Curves
   - Spirals
 - Drawing:
   - More particle options, like speed, colour, offset...
   - Dynamically change data within shape (gradients)
   - Fill in shapes
   - Options for who sees the particle (player specific)
 
 
 ## Syntax
 Currently, there are only a few syntaxes in Skript-Particle. Circles, Lines, rotation expressions, and the draw effect.
 
 ### Shapes
 
 #### Circle
 ```
 [(the|a)] circle [(with|of)] radius %number% [(and|[and] with) [a] step size [of] %-number% [(degrees|radians)]]
 [(the|a)] circle [(with|of)] radius %number% (and|[and] with) [a] particle count [of] %number%
 [(the|a)] circle [(with|of)] radius %number% (and|[and] with) %number% (points|particles)
 ```
 Circles only have a radius and a stepsize/particle count. Step sizes are in degrees by default. If a step size is omitted, the default value will be 12 degrees or 30 particles.
 
 #### Examples
 ```
 the circle with radius 10 
 circle with radius 1 with step size 10 degrees
 a circle of radius 3.25 with 100 particles
 circle radius 3 with step size (3.141 / 2) radians
 ```
 
 #### Line
 ```
 [(the|a)] line (from|between) %vector% (to|and) %vector% [with [a] step size [of] %-number% [meters]]
 [(the|a)] line (from|between) %location% (to|and) %location% [with [a] step size [of] %-number% [meters]]
 [(the|a)] line (in [the]|from) direction %vector% (and|[and] with) length %number% [with [a] step size [of] %-number% [meters]]
 ```
 Lines have a start and end point and a step size, but they're more flexible than most shapes. Since lines are typically drawn from a location to another, rather than centered at some location like more other shapes, they can be defined in a few ways. The first and third syntaxes create relative lines, the first being between two points defined by vectors from a center point, and the second being from the center point in some direction for some length.
 
 The second syntax, though, is from one location to another. Lines defined by this syntax can be treated like a line from the center point to a vector, which would be the vector between the two locations, but they can also be drawn without a center point. In that case the line will simply be drawn between the two locations, with no fuss.
 
 #### Examples
 ```
 line from vector(1,0,0) to vector(0,0,1)
 a line from player's head to player's target block with step size 0.5
 the line in the direction (vector from yaw player's yaw, pitch player's pitch) with length 10
 ```
 
 ### Rotation
 Shapes have two attributes that determine their rotation. A normal vector, which by default points straight up, and an orientation value, which allows rotation about the normal vector. This is subject to change, though, as a yaw/pitch + rotation system may be more intuitive for users.
 
 Shapes can be created with normal vectors and orientation, though this can cause very long lines and is discouraged. The two property syntaxes are as follows:
 
 #### Normal Vector
 ```
 normal [vector] of %shapes%
 %shapes%'[s] normal [vector]
 ```
 These vectors can be set, deleted, manipulated, and rotated like any other vector, and these changes will affect the rotation of the shape. 
 
 #### Orientation
 ```
 orientation of %shapes%
 %shapes%'[s] orientation
 ```
 Orientation is a degree value that corresponding to rotation around the normal vector. I'm considering changing the name but "rotation" is too broad and "yaw" conflicts with the normal idea of yaw. Suggestions welcome.
 
 #### Copying Shapes with New Rotations
 Additionally, one can create a copy of a shape with a new normal vector and/or orientation value with the following syntax. The original shape will not be affected.
 ```
 %shapes% with normal [vector] %vector%
 %shapes% (with orientation [at]|oriented [at|with]) %number% [(degrees|radians)]
 ```
 
 ### Drawing
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
 
 ## Example Code
 Here's a quick example of a "magic circle" particle effect that's easily rotated and moved around:
 ```tcl
on load:
    # three circles
    set {magic-circle::1} to a circle with radius 1.75 with 36 particles
    set {magic-circle::2} to a circle with radius 4 with 180 particles
    set {magic-circle::3} to a circle with radius 4.45 with 200 particles
    
    # two triangles made of 6 lines
    set {_v1} to spherical vector with radius 4, yaw 0, pitch 0
    set {_v2} to spherical vector with radius 4, yaw 120, pitch 0
    set {magic-circle::4} to a line from {_v1} to {_v2} 
    set {magic-circle::5} to a line from {_v1} to {_v2} oriented at 60 degrees
    set {magic-circle::6} to a line from {_v1} to {_v2} oriented at 120 degrees
    set {magic-circle::7} to a line from {_v1} to {_v2} oriented at 180 degrees
    set {magic-circle::8} to a line from {_v1} to {_v2} oriented at 240 degrees
    set {magic-circle::9} to a line from {_v1} to {_v2} oriented at 300 degrees

command magic-circle-1:
    trigger:
        draw {magic-circle::*} using electric spark at player
        
command magic-circle-2:
    trigger:
        loop 360 times:
            # this doesn't change the base shape rotation, as it creates a copy of the base shape
            draw ({magic-circle::*} oriented at loop-number degrees) using electric spark at player
            wait 1 tick
            
command magic-circle-3:
    trigger:
        loop 180 times:
            # these actually change the base shape's rotation, so be careful
            add 3 to yaw of (normal vector of {magic-circle::*})
            add 1 to pitch of (normal vector of {magic-circle::*})
            draw {magic-circle::*} using electric spark at player
            wait 1 tick
        reset (normal vector of {magic-circle::*})
 ```
 
 ![2022-08-28_00 10 57](https://user-images.githubusercontent.com/10354869/187062233-5f51ba7b-60f4-44f8-bf6b-862a4e2381fd.png)


https://user-images.githubusercontent.com/10354869/187062241-d3c51f86-4129-4f8b-9ce3-2d0037779e4e.mp4


