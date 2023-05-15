# skript-particle
 Skript addon for creating complex particle effects with minimal hassle.
 
 **Requires Skript 2.7+, Java 17, Paper 1.17.1+**
 
 Download at the [releases page.](https://github.com/sovdeeth/skript-particle/releases) 

You can find help, give suggestions, or voice complaints in the [Issues tab here](https://github.com/sovdeeth/skript-particle/issues/new/choose), or on the [skript-chat discord here](https://discord.gg/v9dXfENDnk).
 
 Skript-Particle is intended to provide syntax for creating complex shapes with particles. Competent and powerful particle syntaxes exist in addons like SkBee, but creating shapes requires a lot more effort and know-how. 
 They're also inherently slower and less performant than Java, even without considering the usual need to re-calculate the shape each time it's drawn.
 
 The goal for Skript-Particle is to provide easy-to-use, flexible, and powerful syntax to create particle shapes. 
 The basic structure is a set of various shapes, like circles, lines, polygons, and more. These shapes are relative to a center point and aren't positioned anywhere specific. 
 The drawing syntax takes a set of shapes and a location and draws all the shapes relative to that location. 
 This allows the definition of a bunch of shapes that only need to be defined once and can then be drawn in many places and in many rotations. 
 Since shapes can be rotated as a group, it's also very simple to change the rotation and orientation of your shapes with a single line.

Here's some example code:
```
command /magic:
    trigger:
        set {_shapes::outer-circle} to a circle of radius 2.225
        set {_shapes::inner-circle} to a circle of radius 2
        set {_shapes::tiny-circle} to a circle of radius 0.875

        set {_shapes::triangle-1} to a triangle with radius 2
        set {_shapes::triangle-2} to a triangle with radius 2
        rotate shapes {_shapes::triangle-2} around y axis by 60 degrees

        set particle of {_shapes::*} to electric spark

        loop 200 times:
            set {_view} to vector from yaw player's yaw and pitch player's pitch
            set {_yaw} to yaw of {_view}
            # figure out the rotation needed to rotate the shape 
            set {_rotation} to rotation from vector(0, 1, 0) to {_view}
            draw {_shapes::*} at player's eye location ~ {_view}:
                # only happens for this draw call, the original shape is not modified
                # note that this is called once for each shape, hence `drawn shape` and not `drawn shapes`
                rotate shape drawn shape by {_rotation}
                # the shape takes the shortest path to rotate to the desired rotation, but that causes it to appear to rotate as we turn.
                # so we'll correct for it by rotating the shape around the y axis by the yaw of the player
                rotate shape drawn shape around relative y axis by -1 * {_yaw}

            wait 1 tick 
```
 
## Current Features:
- Shapes:
  - Circles, Ellipses, Spheres, and Ellipsoids
  - Cylinders and Elliptical Cylinders
  - Arcs and Spherical Caps
  - Helices
  - Lines
  - 2D Regular and Irregular Polygons and Prisms
  - Regular polyhedra
  - Rectangles and cuboids
- Drawing:
  - Three styles: outline, surface, and filled. Some shapes only support one or two of these styles.
  - Rotation to any orientation.
  - Scaling and offsetting.
  - Drawing multiple shapes at once.
  - Options for who sees the particles (player specific)
  - Custom particle data and density per shape
    - Full support for SkBee's particle data syntaxes
  - Debug axes for clear visualization of the orientation of your shape.
  - Shapes are only calculated when they actually change, so you can draw the same shape many times without any performance hit.
  - Option to make all calculation and drawing synchronous (async by default)
     - Ability to continuously draw a shape for a timespan (async only)
  - Dynamic locations, so particles can follow an entity with no additional user effort
- Particles:
  - Expression for custom particles
  - Section for custom particles

 ## Planned Features:
 - Shapes:
   - Bezier Curves
   - Custom shapes defined by a function
   - "Common but difficult to code" shapes like hearts, stars, and more
 - Drawing:
   - Combining shapes into a custom shape to be drawn as a single shape.
   - Gradient colour fields, with custom colour nodes.
 - Animation:
   - Ability to stretch out drawing a shape over a timespan
   - Set up a pre-made animation in a custom structure, play back with one draw call 
 - Better particle syntax:
   - Particle motion tag, to allow motions based on the shape: inwards, outwards, clockwise, counter-clockwise

 # Syntax
See the github wiki for syntax information.
 
![2022-08-28_00 10 57](https://user-images.githubusercontent.com/10354869/187062233-5f51ba7b-60f4-44f8-bf6b-862a4e2381fd.png)


https://user-images.githubusercontent.com/10354869/187062241-d3c51f86-4129-4f8b-9ce3-2d0037779e4e.mp4


