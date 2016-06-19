package statistics.p4combined;

/**
 * Created by Ryan on 4/3/2016.
 */
public class Formulas {
    public static double inverseNorm(double p) {
        // Start with some reasonably large delta, note that first loop will always result in delta being cut in half, since lastDirection init to 0.
        double delta = 2;
        double z = 0;
        double currentcdf = 0;
        double lastDirection = 0;
        double direction = 0;
        while(delta > 1e-6) {
            currentcdf = normcdf(z);
            if (currentcdf >= p) {
                // too high, move left
                direction = -1;
            } else {
                // too low, move right
                direction = 1;
            }
            z += delta * direction;
            if (direction != lastDirection) {
                // Shifting direction, so cut delta in half
                delta /= 2.0;
                lastDirection = direction;
            }
        }
        return z;
    }

    public static double normcdf(double z){
        double t = (z > 0) ? z : (- z);
        double p = 1 - Math.pow((1 + (t * (0.0498673470 + t * (0.0211410061 + t *
                (0.0032776263 + t * (0.0000380036 + t * (0.0000488906 + t *
                        0.0000053830))))))), -16) / 2;
        return ((z > 0) ? p : (1 - p));
    }
}
