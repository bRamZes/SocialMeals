package nl.miwgroningen.cohort5.socialmeals.seeder;

import nl.miwgroningen.cohort5.socialmeals.dto.*;
import nl.miwgroningen.cohort5.socialmeals.service.CookbookService;
import nl.miwgroningen.cohort5.socialmeals.service.IngredientService;
import nl.miwgroningen.cohort5.socialmeals.service.RatingService;
import nl.miwgroningen.cohort5.socialmeals.service.RecipeService;
import nl.miwgroningen.cohort5.socialmeals.service.implementation.SocialMealsUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author A.H. van Zessen
 */

@Component
public class Seeder {

    private final RecipeService recipeService;
    private final IngredientService ingredientService;
    private final SocialMealsUserDetailService socialMealsUserDetailService;
    private final CookbookService cookbookService;
    private final RatingService ratingService;

    private static final Logger LOGGER = LoggerFactory.getLogger(Seeder.class);

    @Autowired
    public Seeder(RecipeService recipeService,
                  IngredientService ingredientService,
                  SocialMealsUserDetailService socialMealsUserDetailService,
                  CookbookService cookbookService,
                  RatingService ratingService) {
        this.recipeService = recipeService;
        this.ingredientService = ingredientService;
        this.socialMealsUserDetailService = socialMealsUserDetailService;
        this.cookbookService = cookbookService;
        this.ratingService = ratingService;
    }

    @EventListener
    public void seed(ContextRefreshedEvent event) {
        seedUser();
        seedIngredients();
        seedRecipes();
        seedRecipeImages();
        seedIngredientRecipesBabaGanoush();
        seedIngredientRecipesGyoza();
        seedCookbooks();
        seedRatings();
    }

    private void seedUser() {
        if (socialMealsUserDetailService.getAll().size() == 0) {
            socialMealsUserDetailService.addSocialMealsUser("admin", "admin");
            socialMealsUserDetailService.addSocialMealsUser("Joop", "123");
            socialMealsUserDetailService.addSocialMealsUser("Harry", "123");
            socialMealsUserDetailService.addSocialMealsUser("Sara", "123");
            socialMealsUserDetailService.addSocialMealsUser("Douwe", "123");
            socialMealsUserDetailService.addSocialMealsUser("Arlette", "123");
            socialMealsUserDetailService.addSocialMealsUser("Daphne", "123");
            socialMealsUserDetailService.addSocialMealsUser("Victor", "123");
        }
    }

    private void seedRecipes() {

        List<String> steps = new ArrayList<>(babaGanoushSteps());
        List<String> steps2 = new ArrayList<>(List.of("Fry garlic", "and onion", "add everything and put in oven"));
        List<String> steps3 = new ArrayList<>(gyozaSteps());

        SocialMealsUserDTO joop = socialMealsUserDetailService.getUserByUsername("Joop");
        SocialMealsUserDTO victor = socialMealsUserDetailService.getUserByUsername("Victor");
        SocialMealsUserDTO sara = socialMealsUserDetailService.getUserByUsername("Sara");

        recipeService.addNew(new RecipeDTO("Lasagna", steps2, joop));
        recipeService.addNew(new RecipeDTO("Baba Ganoush", steps, joop));
        recipeService.addNew(new RecipeDTO("Poké Bowl", new ArrayList<>(), joop));
        recipeService.addNew(new RecipeDTO("Cheese Pizza", new ArrayList<>(), joop));
        recipeService.addNew(new RecipeDTO("Mac And Cheese", new ArrayList<>(), joop));
        recipeService.addNew(new RecipeDTO("Strawberry Smoothie", new ArrayList<>(), joop));
        recipeService.addNew(new RecipeDTO("Chickpea Dahl", new ArrayList<>(), joop));
        recipeService.addNew(new RecipeDTO("Sweet Potato Curry", new ArrayList<>(), joop));


        recipeService.addNew(new RecipeDTO("Vegan Gyoza", steps3, victor));
        recipeService.addNew(new RecipeDTO("Vegan Pokébowl", new ArrayList<>(), victor));
        recipeService.addNew(new RecipeDTO("Korean Veggie Pancake", new ArrayList<>(), victor));
        recipeService.addNew(new RecipeDTO("Vegan Peanutbutter Bananabread", new ArrayList<>(), victor));
        recipeService.addNew(new RecipeDTO("Vegan Coucous", new ArrayList<>(), victor));
        recipeService.addNew(new RecipeDTO("Vegan Pizza With Tofu", new ArrayList<>(), victor));

        recipeService.addNew(new RecipeDTO("Croque-Monsieur", new ArrayList<>(), sara));
        recipeService.addNew(new RecipeDTO("Daging Bali", new ArrayList<>(), sara));
        recipeService.addNew(new RecipeDTO("Beef Stew", new ArrayList<>(), sara));
        recipeService.addNew(new RecipeDTO("Rack of Lamb", new ArrayList<>(), sara));
        recipeService.addNew(new RecipeDTO("Sweet Pea Fish Pie", new ArrayList<>(), sara));
        recipeService.addNew(new RecipeDTO("Cheese Fondue", new ArrayList<>(), sara));

    }

    private void seedRecipeImages() {

        File babaGanoushImage = new File("src/main/resources/images/baba.ganoush.jpg");
        File gyozaImage = new File("src/main/resources/images/gyoza.jpeg");

        RecipeDTO babaGanoush = recipeService.findByUrlId(Long.valueOf(5002));
        RecipeDTO gyoza = recipeService.findByUrlId(Long.valueOf(5009));

        babaGanoush.setRecipeImage(imageToByteArray(babaGanoushImage));
        gyoza.setRecipeImage(imageToByteArray(gyozaImage));

        recipeService.updateRecipeWithImage(recipeService.findByUrlId(Long.valueOf(5002)), babaGanoush);
        recipeService.updateRecipeWithImage(recipeService.findByUrlId(Long.valueOf(5009)), gyoza);
    }

    private List<String> babaGanoushSteps() {
        List<String> steps = new ArrayList<>();

        steps.add("Preheat the oven to 230 degrees Celcius. Line a baking sheet with parchment paper. " +
                "Halve the eggplants lengthwise, brush them lightly with olive oil and roast for 30 to 40 minutes.");
        steps.add("Set the eggplants aside for a few minutes. Flip them over and scoop out the flesh with a spoon. " +
                "Discard the skin.");
        steps.add("Strain the eggplant flesh over a mixing bowl. " +
                "Let the eggplant rest for a few minutes and try again to remove more moisture. " +
                "Discard the moisture afterwards. ");
        steps.add("Clean and dry the mixing bowl and add the eggplant to it. " +
                "Add the garlic and lemon juice and stir with a fork until the eggplant breaks down.");
        steps.add("Add tahini and mix well. Slowly add olive oil while stirring. " +
                "Continue stirring until the mixture is pale and creamy.");
        steps.add("Add parsley, salt and cumin to the mixture. " +
                "Season with more salt and lemon juice if you like.");
        steps.add("Serve the baba ganoush with a light drizzle of olive oil " +
                "and some sprinkled parsley and smoked paprika on top.");

        return steps;
    }

    private List<String> gyozaSteps() {
        List<String> steps = new ArrayList<>();

        steps.add("Combine everything except the spring onions, flour, salt and chilli pwoder in a bowl and mix well.");
        steps.add("Stir-fry the mixture in a frying pan over a mediumhigh heat.");
        steps.add("The mixture should be dry after about 10 minutes. Turn off the heat and mix in the spring onion and flour");
        steps.add("leave to cool and break the tofu into little crumbs.");
        steps.add("Take a wrapper and place a small amount of filling at the center. " +
                "Fold the wrapper in half and start making a pleat once every 1/4 part of the wrapper." +
                "Continue until sealed.");
        steps.add("Cook the gyoza until golden brown, for about 3 minutes.");

        return steps;
    }


    private void seedIngredients() {
        ingredientService.addNew(new IngredientDTO("Tomato"));
        ingredientService.addNew(new IngredientDTO("Eggplant"));
        ingredientService.addNew(new IngredientDTO("Garlic"));
        ingredientService.addNew(new IngredientDTO("Coriander"));
        ingredientService.addNew(new IngredientDTO("Rice"));
        ingredientService.addNew(new IngredientDTO("Broth"));
        ingredientService.addNew(new IngredientDTO("Pepper"));
        ingredientService.addNew(new IngredientDTO("Olive oil"));
        ingredientService.addNew(new IngredientDTO("Tahini"));
        ingredientService.addNew(new IngredientDTO("Lemon"));
        ingredientService.addNew(new IngredientDTO("Cumin"));
        ingredientService.addNew(new IngredientDTO("Parsley"));
        ingredientService.addNew(new IngredientDTO("Smoked paprika"));
        ingredientService.addNew(new IngredientDTO("Flour"));
        ingredientService.addNew(new IngredientDTO("Eggs"));
        ingredientService.addNew(new IngredientDTO("Milk"));
        ingredientService.addNew(new IngredientDTO("Dates"));
        ingredientService.addNew(new IngredientDTO("Mushrooms"));
        ingredientService.addNew(new IngredientDTO("Green Beans"));
        ingredientService.addNew(new IngredientDTO("Broccoli"));
        ingredientService.addNew(new IngredientDTO("Carrots"));

        ingredientService.addNew(new IngredientDTO("Tofu"));
        ingredientService.addNew(new IngredientDTO("gyoza wraps"));
        ingredientService.addNew(new IngredientDTO("kimchi"));
        ingredientService.addNew(new IngredientDTO("sesame seeds"));
        ingredientService.addNew(new IngredientDTO("sesame oil"));
        ingredientService.addNew(new IngredientDTO("ginger"));
        ingredientService.addNew(new IngredientDTO("spring onions"));
        ingredientService.addNew(new IngredientDTO("chilli powder"));
    }

    private void seedIngredientRecipesBabaGanoush() {
        List<IngredientRecipeDTO> ingredientRecipeList = new ArrayList<>();
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("tomato"),
                        recipeService.findByUrlId(Long.valueOf(5001)),
                        5,
                        "pcs"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("eggplant"),
                        recipeService.findByUrlId(Long.valueOf(5002)),
                        6,
                        "pcs"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("Olive oil"),
                        recipeService.findByUrlId(Long.valueOf(5002)),
                        3,
                        "tbs"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("Parsley"),
                        recipeService.findByUrlId(Long.valueOf(5002)),
                        2,
                        "ts"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("Smoked paprika"),
                        recipeService.findByUrlId(Long.valueOf(5002)),
                        1,
                        "ts"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("tahini"),
                        recipeService.findByUrlId(Long.valueOf(5002)),
                        2,
                        "tbs"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("cumin"),
                        recipeService.findByUrlId(Long.valueOf(5002)),
                        10,
                        "g"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("lemon"),
                        recipeService.findByUrlId(Long.valueOf(5002)),
                        5,
                        "squeezes"));

        recipeService.addIngredientsToRecipe(ingredientRecipeList);

    }

    private void seedIngredientRecipesGyoza() {
        List<IngredientRecipeDTO> ingredientRecipeList = new ArrayList<>();
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("gyoza wraps"),
                        recipeService.findByUrlId(Long.valueOf(5009)),
                        10,
                        "pcs"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("tofu"),
                        recipeService.findByUrlId(Long.valueOf(5009)),
                        200,
                        "g"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("kimchi"),
                        recipeService.findByUrlId(Long.valueOf(5009)),
                        120,
                        "g"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("garlic"),
                        recipeService.findByUrlId(Long.valueOf(5009)),
                        2,
                        "cloves"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("ginger"),
                        recipeService.findByUrlId(Long.valueOf(5009)),
                        10,
                        "g"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("sesame oil"),
                        recipeService.findByUrlId(Long.valueOf(5009)),
                        1,
                        "tbs"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("sesame seeds"),
                        recipeService.findByUrlId(Long.valueOf(5009)),
                        1,
                        "tbs"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("spring onions"),
                        recipeService.findByUrlId(Long.valueOf(5009)),
                        2,
                        "pcs"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("flour"),
                        recipeService.findByUrlId(Long.valueOf(5009)),
                        1,
                        "tbs"));
        ingredientRecipeList.add(
                new IngredientRecipeDTO(ingredientService.findByIngredientName("chilli powder"),
                        recipeService.findByUrlId(Long.valueOf(5009)),
                        0.2,
                        "ts"));

        recipeService.addIngredientsToRecipe(ingredientRecipeList);
    }

    private void seedCookbooks() {
        SocialMealsUserDTO socialMealsUserDTO = socialMealsUserDetailService.getUserByUsername("Joop");

        cookbookService.addNew(new CookbookDTO("Breakfast Ideas", socialMealsUserDTO, new ArrayList<>()));
        cookbookService.addNew(new CookbookDTO("Summer Recipes", socialMealsUserDTO, new ArrayList<>()));
        cookbookService.addNew(new CookbookDTO("Mexican Dishes", socialMealsUserDTO, new ArrayList<>()));

        CookbookDTO cookbookDTO = new CookbookDTO("Libanese Recipes", socialMealsUserDTO, new ArrayList<>());
        cookbookService.addNew(cookbookDTO);
        RecipeDTO recipeDTO = recipeService.findByUrlId(Long.valueOf(5001));
        cookbookService.addRecipeDTO(cookbookDTO, recipeDTO);

        CookbookDTO cheeseRecipes = new CookbookDTO("Cheesy Recipes", socialMealsUserDTO, new ArrayList<>());
        cookbookService.addNew(cheeseRecipes);

        RecipeDTO macAndCheese = recipeService.findByUrlId(Long.valueOf(5005));
        RecipeDTO croqueMonsieur = recipeService.findByUrlId(Long.valueOf(5015));
        RecipeDTO cheesePizza = recipeService.findByUrlId(Long.valueOf(5004));
        RecipeDTO cheeseFondue = recipeService.findByUrlId(Long.valueOf(5020));

        cookbookService.addRecipeDTO(cheeseRecipes, macAndCheese);
        cookbookService.addRecipeDTO(cheeseRecipes, croqueMonsieur);
        cookbookService.addRecipeDTO(cheeseRecipes, cheesePizza);
        cookbookService.addRecipeDTO(cheeseRecipes, cheeseFondue);
    }

    private void seedRatings() {
        SocialMealsUserDTO joop = socialMealsUserDetailService.getUserByUsername("Joop");
        SocialMealsUserDTO harry = socialMealsUserDetailService.getUserByUsername("Harry");
        SocialMealsUserDTO sara = socialMealsUserDetailService.getUserByUsername("Sara");
        SocialMealsUserDTO douwe = socialMealsUserDetailService.getUserByUsername("Douwe");
        SocialMealsUserDTO arlette = socialMealsUserDetailService.getUserByUsername("Arlette");
        SocialMealsUserDTO daphne = socialMealsUserDetailService.getUserByUsername("Daphne");
        SocialMealsUserDTO victor = socialMealsUserDetailService.getUserByUsername("Victor");

        RecipeDTO recipeDTO = recipeService.findByUrlId(Long.valueOf(5002));
        RecipeDTO recipeDTO2 = recipeService.findByUrlId(Long.valueOf(5003));
        RecipeDTO recipeDTO3 = recipeService.findByUrlId(Long.valueOf(5004));
        RecipeDTO recipeDTO4 = recipeService.findByUrlId(Long.valueOf(5009));

        ratingService.addNew(new RatingDTO(4, recipeDTO, joop));
        ratingService.addNew(new RatingDTO(3, recipeDTO, harry));
        ratingService.addNew(new RatingDTO(2, recipeDTO, sara));
        ratingService.addNew(new RatingDTO(5, recipeDTO, douwe));
        ratingService.addNew(new RatingDTO(5, recipeDTO, arlette));
        ratingService.addNew(new RatingDTO(4, recipeDTO, daphne));
        ratingService.addNew(new RatingDTO(5, recipeDTO, victor));

        ratingService.addNew(new RatingDTO(3, recipeDTO2, joop));
        ratingService.addNew(new RatingDTO(2, recipeDTO2, harry));
        ratingService.addNew(new RatingDTO(1, recipeDTO2, sara));
        ratingService.addNew(new RatingDTO(4, recipeDTO2, douwe));
        ratingService.addNew(new RatingDTO(3, recipeDTO2, daphne));


        ratingService.addNew(new RatingDTO(4, recipeDTO3, joop));
        ratingService.addNew(new RatingDTO(3, recipeDTO3, harry));
        ratingService.addNew(new RatingDTO(5, recipeDTO3, sara));

        ratingService.addNew(new RatingDTO(3, recipeDTO4, harry));
    }

    private byte[] imageToByteArray(File image) {

        byte[] data = null;

        try {
            BufferedImage bImage = ImageIO.read(image);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bImage, "jpg", outputStream);
            data = outputStream.toByteArray();
        } catch (IOException e) {
            LOGGER.error("Failed to read image {}", image.getAbsolutePath(), e);
        }

        return data;
    }
}
