// Joel Canonico T00686800 COMP 3710 Applied AI Computational Art Project

// all required import statements 
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Random;

// only class of the program, the name essentially describes what the program does --> shifts any input image to an output image with more of a 'pop' art style
public class GAImageGenreChange
{
    // constants for genetic algorithm parameters
    private static final int POPULATION_SIZE = 50;
    private static final int MAX_GENERATIONS = 100;
    private static final double MUTATION_RATE = 0.01; // mutation rate, with 0 being an exact replica

    // method to load the image from the file
    private static BufferedImage loadImage(String filePath) 
    {
        // attempts to read file
        try
        {
            return ImageIO.read(new File(filePath));
        } 
        
        // provide error message if the file cannot be read
        catch (IOException e) 
        {
            e.printStackTrace();
            return null;
        }
    }

    // save the altered image to a file
    private static void saveImage(BufferedImage image, String outputPath) 
    {
        // attempts to save the output image to the specified path (my desktop in this case)
        try 
        {
            File output = new File(outputPath);
            ImageIO.write(image, "png", output);
            System.out.println("Image saved to: " + output.getAbsolutePath());

        } 
        // if it is unable to save at the specified path, the exception stack trace and an error message is provided
        catch (IOException e) 
        {
            e.printStackTrace();
            System.err.println("Failed to save image at path: " + outputPath);
        }
    }    

    // fitness method to evaluate the quality of an image - calculate sum of all pixel rgb values
    private static double determineFitness(BufferedImage image) 
    {
        // calculate the sum of pixel rgb values/intensities 
        double fitness = 0;

        // loop through the x and y pixel coordinates of the image, add the pixel rgb values to the total fitness value, higher = better in this case
        for (int y = 0; y < image.getHeight(); y++) 
        {
            for (int x = 0; x < image.getWidth(); x++) 
            {
                fitness += image.getRGB(x, y) & 0xFF;
            }
        }
        return fitness; // return the current fitness
    }

    // evolveImage method which performs essentially all of the GA methods in altering the image, can be seen as the evolutionary loop of the GA
    private static BufferedImage evolveImage(BufferedImage originalImage) 
    {
        // initializes the population of altered images
        BufferedImage[] population = new BufferedImage[POPULATION_SIZE];

        // loops through the length of the population
        for (int i = 0; i < POPULATION_SIZE; i++) 
        {
            // implement image alteration method, changing pixel values, save as a possibility in the popluation
            population[i] = alterImage(originalImage);
        }

        // loop that runs through evolution
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) 
        {
            // storing each fitness value in an array size of the population
            double[] fitnessValues = new double[POPULATION_SIZE];

            // loop through the entire population size, storing the fitness value of each individual/image in the array defined above
            for (int i = 0; i < POPULATION_SIZE; i++) 
            {
                fitnessValues[i] = determineFitness(population[i]);
            }

            // select the individuals/images for reproduction based on fitness (most fit)
            BufferedImage[] newPopulation = new BufferedImage[POPULATION_SIZE];

            // applying the selection method to define two parents and then do a crossover to produce new, more 'fit' images for the new population
            for (int i = 0; i < POPULATION_SIZE; i++) 
            {
                int p1Index = selection(fitnessValues);
                int p2Index = selection(fitnessValues);
                newPopulation[i] = crossover(population[p1Index], population[p2Index]);
            }

            // apply the mutation method to each individual/image in the new population
            for (int i = 0; i < POPULATION_SIZE; i++) 
            {
                if (Math.random() < MUTATION_RATE) 
                {
                    newPopulation[i] = mutate(newPopulation[i]);
                }
            }

            // update the population for the next generation
            population = newPopulation;
        }

        // return the best individual from the final generation
        return findBestImage(population);
    }

    // alter the image by randomly changing pixel values
    private static BufferedImage alterImage(BufferedImage originalImage) 
    {
        // get the width, height, and type of image
        BufferedImage alteredImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());

        // loop through all of the x and y values of the inputted image (should be 512x512 for the project but can be any)
        for (int y = 0; y < originalImage.getHeight(); y++) 
        {
            for (int x = 0; x < originalImage.getWidth(); x++) 
            {
                // get the original rgb image values and shifts the red, green 16 and 8 bits to the right, respectively, to isolate them
                int rgb = originalImage.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
    
                // limiting colour values from 0 to 255 in an attempt to avoid issues
                red = Math.min(255, red);
                green = Math.min(255, green);
                blue = Math.min(255, blue);
    
                // stores the new RGB values and sets the altered image RGB values to the new ones in the corresponding x, y pixel point
                int newRGB = (red << 16) | (green << 8) | blue; // shifts red and green values 16 and 8 bits to the left, respectively, and are combined with the blue value using the or operation
                alteredImage.setRGB(x, y, newRGB);
            }
        }

        return alteredImage; // returns the new altered image with the colour shifts
    }

    // method for selecting a parent based on fitness values
    private static int selection(double[] fitnessValues) 
    {
        // using roulette wheel selection
        double totalFitness = 0;

        // add up the total fitness through a loop sum
        for (double fitness : fitnessValues) 
        {
            totalFitness += fitness;
        }

        // initializing a random value which will be used to select a parent based on their fitness 
        double randomValue = Math.random() * totalFitness;
        double cumulativeFitness = 0;

        // loop through the entire population, adding the fitness values while looking to select a parent if/when the cumulativeFitness exceeds the randomValue
        for (int i = 0; i < POPULATION_SIZE; i++) 
        {
            cumulativeFitness += fitnessValues[i];

            if (cumulativeFitness >= randomValue) 
            {
                return i; // return index of the chosen parent
            }
        }

        return POPULATION_SIZE - 1; // this is the default case but should not usually reach here
    }

    // method for crossover (combine genetic material of two parents)
    private static BufferedImage crossover(BufferedImage p1, BufferedImage p2) 
    {
        // combine pixel values from two parents
        BufferedImage child = new BufferedImage(p1.getWidth(), p1.getHeight(), p1.getType());

        // loop through every pixel in the image while setting new colour values
        for (int y = 0; y < p1.getHeight(); y++) 
        {
            for (int x = 0; x < p1.getWidth(); x++) 
            {
                if (Math.random() < 0.5) 
                {
                    child.setRGB(x, y, p1.getRGB(x, y)); // set the rgb to parent 1 rgb values
                } 
                else 
                {
                    child.setRGB(x, y, p2.getRGB(x, y)); // else, set the rgb to parent 2 rgb values
                }
            }
        }
        return child; // return the produced child image from the two parents combined
    }

    // mutation method which changes rgb colours of each pixel of the image
    private static BufferedImage mutate(BufferedImage receivedImage) 
    {
        int width = receivedImage.getWidth();
        int height = receivedImage.getHeight();
    
        // creates a new mutatedImage variable that uses the same width, height, and overall type of image which is what 'TYPE_INT_RGB is used for'
        BufferedImage mutatedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    
        // loop through each pixel (x,y) values of the image while receiving, increasing/decreasing RGB values and eventually applying these changes to the mutatedImage variable
        for (int y = 0; y < height; y++) 
        {
            for (int x = 0; x < width; x++) 
            {
                // get the rgb values of the received image
                int rgb = receivedImage.getRGB(x, y); 
    
                // extract the red, green, and blue values
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
    
                // change RGB colors for the pop art style effect
                red = Math.min(255, red + 1); // increase red color
                green = Math.min(255, green - 1); // decrease green color
                blue = Math.min(255, blue + 1); // increase blue color
    
                // reconstructs the new rgb values and applies them to the mutated image
                int newRGB = (red << 16) | (green << 8) | blue;
                mutatedImage.setRGB(x, y, newRGB);
            }
        }
        return mutatedImage; // return the mutated image with the slight rgb colour changes
    }    

    // method for getting the best image from a population
    private static BufferedImage findBestImage(BufferedImage[] population) 
    {
        // intitializing variables that will be used in the loop, one to hold the bestFitness and another to hold the bestImage after the loop is done
        double bestFitness = 0;
        BufferedImage bestImage = null;

        // for every image in the population array, look at the fitness, see if it is more than the current bestFitness, if so update the bestFitness and bestImage variables
        for (BufferedImage image : population) 
        {
            double fitness = determineFitness(image);

            if (fitness > bestFitness) 
            {
                bestFitness = fitness;
                bestImage = image;
            }
        }
        return bestImage; // return the bestImage after all of the images in the population have been evaluated, comparing fitnesses
    }

    // main method to run the program
    public static void main(String[] args) 
    {
        // path where the input image is located (my laptop desktop)
        String inputImagePath = "C:\\Users\\joelc\\OneDrive\\Desktop\\image.png";
    
        BufferedImage originalImage = loadImage(inputImagePath);
    
        // provide an output image for each generation 
        for (int i = 0; i < MAX_GENERATIONS; i++)
        {
            // performs the GA for image alteration
            BufferedImage evolvedImage = evolveImage(originalImage);

            // path where the image of each generation will be saved (my laptop desktop)
            String outputImagePath = "C:\\Users\\joelc\\OneDrive\\Desktop\\alteredImage" + (i+1) + ".png";
            saveImage(evolvedImage, outputImagePath);

            // print the fitness of each image generation (used to find the average increase)
            System.out.println("Image Generation " + (i+1) + " Fitness: " + determineFitness(evolvedImage));

            // sets the original image to the most recently evolved one for next iteration
            originalImage = evolvedImage;
        }
    }
}