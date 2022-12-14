package main.rendering;

import main.geometry.Solid;
import main.maths.*;
import main.scene.Camera;
import main.scene.PointLight;
import main.scene.Scene;
import main.utils.VectorColor;

import java.util.List;

public class Renderer {

    private static double MAX_RAY_DEPTH = 5;


    private Renderer() {
    }

    public static int tracePixel(Camera camera, Scene scene, int x, int y) {
        // Cast ray and check if it intersects with something
        FullRay ray = camera.getRayFromPixel(x, y);// gets the ray with the coörds of the virtual screen that's
        // equal to the x and y pixel of the image
        RayHit hit = ray.castRay(scene.getGeometry());

        // If the ray intersects with something, write coordinates in console and call
        // the calculateLight(RayHit hit) method with the current rayhit
        // Finally set that pixel with the final color in the image buffer
        if (hit != null) {

                VectorColor finalColor = calculateLight(hit, scene, 0);
                return finalColor.getJavaColor().getRGB();

        } else {
            // if there is no intersection then it will color x and y black
            return 000000;
        }
    }

    // This method checks wheter or not a pixel should be lit up by casting a shadow
    // ray to each light source
    // First it puts the lights and contactpoint in variables and makes a variable
    // for the final output
    public static VectorColor calculateLight(RayHit hit, Scene scene, double rayDepth) {

        // Stopping conditions for when this function is called recursively
        // Stop when max raydepth has been reached
        if (rayDepth == MAX_RAY_DEPTH) {
            return new VectorColor(new Vector3(0, 0, 0));
        }

        // Stop if nothing has been hit
        if (hit == null) {
            return new VectorColor(new Vector3(0, 0, 0));
        }

        // Retrieve scene lights
        List<PointLight> lights = scene.getLights();

        // Get hit position and object
        Vector3 hitPos = hit.getContactPoint();
        Solid hitSolid = hit.getHitSolid();
        Vector3 fullRayDir = hit.getRay().getDirection();

        // Create colors later to be used for reflective calculations and diffuse
        // calculations
        VectorColor reflColor = new VectorColor(new Vector3(0, 0, 0));
        VectorColor diffColor = new VectorColor(new Vector3(0, 0, 0));

        // Calculate the light for the given contactpoint for each light and check if it
        // can see the light
        for (int i = 0; i < lights.size(); i++) {

            // Get the light position and the direction towards the light
            PointLight light = lights.get(i);
            Vector3 lightPos = light.getPosition();
            Vector3 lightDir = lightPos.sub(hitPos).normalise();

            // First calculate the intensity of the color of the pixel according to the
            // intensity of the lightsource and the distance of the lightsource to the
            // contactpoint
            double lightIntensity = light.getIntensity() / Math.pow(hitPos.distance(lightPos), 2);

            // Get contactpoint solid, get material of that then get the color of it and
            // then get the vector of that, divide it by 255 to get 0-1 rgb values
            VectorColor hitColor = new VectorColor(hit.getHitSolid().getMaterial().getColor().getVector());

            // Then multiply it by the light intensity
            hitColor.setColor(hitColor.getVector().multi(lightIntensity));

            // Then do the same for the color of the light and add the result to the
            // reflection vector
            VectorColor lightColor = new VectorColor(light.getColor().getVector().multi(lightIntensity));
            VectorColor reflection = hitColor.addVectorColor(lightColor);

            // Check if object is reflective and send a reflective ray if it is
            if (hitSolid.getMaterial().getReflectivity() > 0.0) {

                // Calculate new ray direction
                Vector3 reflectedRayDir = fullRayDir.sub(hit.getNormal().multi(2 * fullRayDir.dot(hit.getNormal())));

                // Cast a new ray for the reflection
                FullRay reflectedRay = new FullRay(reflectedRayDir, hitPos);
                RayHit reflectedHit = reflectedRay.castRay(scene.getGeometry());

                // Add one step to the raydepth
                rayDepth = rayDepth + 1;

                // Start recursive calculation for the reflection
                // Multiply the result by the reflectivity of the material
                reflColor = new VectorColor(reflection.addVectorColor(calculateLight(reflectedHit, scene, rayDepth))
                        .getVector().multi(hitSolid.getMaterial().getReflectivity()));

                // Check if object refracts light
                if (hitSolid.getMaterial().getIor() > 1.0) {

                    // Get ior of material
                    double ior = hitSolid.getMaterial().getIor();

                    // Calculate fresnel equations
                    double kr = RefractionMath.fresnel(fullRayDir, hit.getNormal(), ior);

                    // Check if ray is outside surface
                    boolean outside = fullRayDir.dot(hit.getNormal()) < 0;

                    // Bias to offset the ray and avoid shadow acne-like effects
                    Vector3 bias = hit.getNormal().multi(Constants.EPSILON);

                    // If the result of the fresnel equations is below 1 calculate refraction
                    if (kr < 1) {

                        // Calculate refraction direction and set origin
                        Vector3 refractDir = RefractionMath.refract(fullRayDir, hit.getNormal(), ior).normalise();
                        Vector3 refractOrigin = hitPos;

                        // Offset origin with bias
                        if (outside) {
                            refractOrigin = refractOrigin.sub(bias);
                        } else {
                            refractOrigin = refractOrigin.add(bias);
                        }

                        // Create refraction ray and cast it
                        FullRay refractRay = new FullRay(refractDir, refractOrigin);
                        RayHit refractHit = refractRay.castRay(scene.getGeometry());

                        // Mix reflection color with refraction color
                        reflColor = new VectorColor(reflColor.getVector().multi(kr)).addVectorColor(
                                new VectorColor(calculateLight(refractHit, scene, rayDepth).getVector().multi(1 - kr)));
                    }
                }
            }

            // Check if reflectivity of object is lower then 1
            // If it is 1, diffuse doesn't need to be calculated anyways
            if (hitSolid.getMaterial().getReflectivity() < 1.0) {

                // Cast a shadowray to the light source
                ShadowRay shaRay = new ShadowRay(lightDir, hitPos);
                boolean shadowHit = shaRay.castRay(scene.getGeometry());

                // If it doesn't intersect with anything calculate light and color
                // Else return black color
                if (!shadowHit) {

                    // Calculate the angle at which the light hits the contact point
                    Vector3 lightAngleDir = hitPos.sub(lightPos);
                    double lightAngle = Math.max(0.01, hit.getNormal().dot(lightAngleDir));

                    // Divide the reflection by the angle of the light and multiply it by 255 to get
                    // 255 rgb values again
                    VectorColor reflectionFinal = new VectorColor(reflection.getVector().divide(lightAngle));

                    // Multiply the resulting color with 1 - the reflectivity of the object
                    diffColor = new VectorColor(reflection.addVectorColor(reflectionFinal).getVector()
                            .multi(1 - hitSolid.getMaterial().getReflectivity()));
                } else {
                    // If there is a hit return gray
                    // we do this because it allows us to disambiguate shadows from the background
                    return new VectorColor(new Vector3(40, 40, 40));
                }
            }
        }
        // Finally return the final result after all the lights have been calculated
        return diffColor.addVectorColor(reflColor);
    }







}