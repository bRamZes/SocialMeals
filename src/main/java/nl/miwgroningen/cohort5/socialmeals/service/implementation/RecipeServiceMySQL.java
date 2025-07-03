package nl.miwgroningen.cohort5.socialmeals.service.implementation;


import nl.miwgroningen.cohort5.socialmeals.dto.IngredientDTO;
import nl.miwgroningen.cohort5.socialmeals.dto.IngredientRecipeDTO;
import nl.miwgroningen.cohort5.socialmeals.dto.RecipeDTO;
import nl.miwgroningen.cohort5.socialmeals.model.*;
import nl.miwgroningen.cohort5.socialmeals.repository.IngredientRecipeRepository;
import nl.miwgroningen.cohort5.socialmeals.repository.RecipeRepository;
import nl.miwgroningen.cohort5.socialmeals.repository.SocialMealsUserRepository;
import nl.miwgroningen.cohort5.socialmeals.service.IngredientService;
import nl.miwgroningen.cohort5.socialmeals.service.RecipeService;
import nl.miwgroningen.cohort5.socialmeals.service.dtoconverter.IngredientRecipeConverter;
import nl.miwgroningen.cohort5.socialmeals.service.dtoconverter.RecipeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author A.H. van Zessen
 *
 * Collects and stores Recipes in the MySQL Database.
 */

@Service
public class RecipeServiceMySQL implements RecipeService {
    private static final long DEFAULT_URL_ID = 5000;

    private final RecipeRepository recipeRepository;
    private final IngredientRecipeRepository ingredientRecipeRepository;
    private final SocialMealsUserRepository socialMealsUserRepository;

    private final IngredientService ingredientService;
    private final SocialMealsUserDetailService socialMealsUserDetailService;

    private final RecipeConverter recipeConverter;
    private final IngredientRecipeConverter ingredientRecipeConverter;

    @Autowired
    public RecipeServiceMySQL(RecipeRepository recipeRepository,
                              IngredientRecipeRepository ingredientRecipeRepository,
                              SocialMealsUserRepository socialMealsUserRepository,

                              IngredientService ingredientService,
                              SocialMealsUserDetailService socialMealsUserDetailService) {
        this.recipeRepository = recipeRepository;
        this.ingredientRecipeRepository = ingredientRecipeRepository;
        this.socialMealsUserRepository = socialMealsUserRepository;

        this.ingredientService = ingredientService;
        this.socialMealsUserDetailService = socialMealsUserDetailService;

        recipeConverter = new RecipeConverter();
        ingredientRecipeConverter =
                new IngredientRecipeConverter(ingredientService);
    }

    @Override
    public List<RecipeDTO> getAll() {
        List<Recipe> recipeList = recipeRepository.findAll();
        List<RecipeDTO> recipeDTOList = recipeConverter.toListDTO(recipeList);

        for (RecipeDTO recipeDTO : recipeDTOList) {
            recipeDTO.setAverageRating(getAverageRatingRecipe(recipeDTO));
            recipeDTO.setNumberOfRatings(getNumberOfRatingsRecipe(recipeDTO));
        }
        return recipeDTOList;
    }

    @Override
    public RecipeDTO addNew(RecipeDTO recipeDTO) {
        recipeDTO.setUrlId(findNextRecipeId());
        formatRecipeName(recipeDTO);

        SocialMealsUser socialMealsUser = socialMealsUserDetailService.getUserByDTO(recipeDTO.getSocialMealsUserDTO());
        Recipe recipe = recipeConverter.fromDTO(recipeDTO, socialMealsUser);

        recipeRepository.save(recipe);
        return recipeDTO;
    }

    @Override
    public void updateRecipe(RecipeDTO oldRecipeDTO, RecipeDTO updatedRecipeDTO) {
        formatRecipeName(updatedRecipeDTO);
        Recipe oldRecipe = getRecipeByRecipeDTO(oldRecipeDTO);
        Recipe newRecipe = recipeConverter.fromDTO(oldRecipe, updatedRecipeDTO);

        recipeRepository.save(newRecipe);
    }

    @Override
    public void updateRecipeWithImage(RecipeDTO oldRecipeDTO, RecipeDTO updatedRecipeDTO) {

        Recipe oldRecipe = getRecipeByRecipeDTO(oldRecipeDTO);
        Recipe newRecipe = recipeConverter.fromDTOWithImage(oldRecipe, updatedRecipeDTO);

        recipeRepository.save(newRecipe);
    }

    @Override
    public RecipeDTO deleteRecipe(RecipeDTO recipeDTO) {
        Recipe recipe = getRecipeByRecipeDTO(recipeDTO);

        if (recipe != null) {
            recipeRepository.delete(recipe);
        }
        return recipeDTO;
    }

    @Override
    public RecipeDTO findByUrlId(Long urlId) {
        Optional<Recipe> recipe = recipeRepository.findByUrlId(urlId);
        if (recipe.isEmpty()) {
            return null;
        }
        RecipeDTO recipeDTO = recipeConverter.toDTO(recipe.get());

        recipeDTO.setAverageRating(getAverageRatingRecipe(recipeDTO));
        recipeDTO.setNumberOfRatings(getNumberOfRatingsRecipe(recipeDTO));

        return recipeDTO;
    }

    @Override
    public void addIngredientsToRecipe(List<IngredientRecipeDTO> ingredientRecipeDTOS) {
        for (IngredientRecipeDTO ingredientRecipeDTO : ingredientRecipeDTOS) {
            addIngredientToRecipe(ingredientRecipeDTO);
        }
    }

    @Override
    public void addIngredientToRecipe(IngredientRecipeDTO ingredientRecipeDTO) {
        Recipe recipe = getRecipeByRecipeDTO(ingredientRecipeDTO.getRecipeDTO());
        ingredientRecipeRepository.save(ingredientRecipeConverter.fromDTO(ingredientRecipeDTO, recipe));
    }

    @Override
    public void deleteIngredientFromRecipe(IngredientRecipe ingredientRecipe) {
        ingredientRecipeRepository.delete(ingredientRecipe);
    }

    public IngredientRecipe getIngredientRecipeByNameAndUrlId(String ingredientName, Long urlId) {

        Optional <Recipe> recipe = recipeRepository.findByUrlId(urlId);
        if (recipe.isEmpty()) {
            return null;
        }

        List<IngredientRecipe> ingredientRecipes = ingredientRecipeRepository.findIngredientRecipeByRecipe(recipe.get());
        IngredientRecipe ingredientRecipe = ingredientRecipes.stream()
                .filter(x -> ingredientName.equals(x.getIngredient().getIngredientName()))
                .findAny()
                .orElse(null);

        return ingredientRecipe;
    }

    @Override
    public List<IngredientRecipeDTO> getIngredientRecipesByRecipeUrlId(Long urlId) {

        Optional<Recipe> recipe = recipeRepository.findByUrlId(urlId);

        if (recipe.isEmpty()) {
            return null;
        }

        List<IngredientRecipe> ingredientRecipeList = ingredientRecipeRepository.findIngredientRecipeByRecipe(recipe.get());

        return ingredientRecipeConverter.toListDTO(ingredientRecipeList);
    }

    @Override
    public List<IngredientDTO> getRemainingIngredientsByUrlId(Long urlId) {
        List<IngredientDTO> allIngredients = ingredientService.getAll();
        List<IngredientRecipeDTO> presentIngredientRecipes = getIngredientRecipesByRecipeUrlId(urlId);
        List<IngredientDTO> presentIngredients = getIngredientsByIngredientRecipes(presentIngredientRecipes);

        return allIngredients.stream()
                .filter(ingredient -> !presentIngredients.contains(ingredient))
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeDTO> getRecipesByUsername(String username) {
        Optional<SocialMealsUser> user = socialMealsUserRepository.findByUsername(username);

        List<Recipe> recipes = user.map(socialMealsUser ->
                recipeRepository.findRecipesBySocialMealsUser(socialMealsUser))
                .orElse(List.of());

        return recipeConverter.toListDTO(recipes);
    }

    public Recipe getRecipeByRecipeDTO(RecipeDTO recipeDTO) {
        Optional<Recipe> recipe = recipeRepository.findByUrlId(recipeDTO.getUrlId());
        return recipe.orElse(null);
    }

    @Override
    public List<Long> search(String keyword) {
        return recipeRepository.search(keyword);
    }

    private Integer getAverageRatingRecipe(RecipeDTO recipeDTO) {
        Recipe recipe = getRecipeByRecipeDTO(recipeDTO);
        double averageRating = recipe.getRatings().stream().mapToDouble(Rating::getStars).average().orElse(Double.NaN);
        return (int) Math.round(averageRating);
    }

    private Integer getNumberOfRatingsRecipe(RecipeDTO recipeDTO) {
        Recipe recipe = getRecipeByRecipeDTO(recipeDTO);

        return recipe.getRatings().size();
    }

    private List<IngredientDTO> getIngredientsByIngredientRecipes(List<IngredientRecipeDTO> ingredientRecipes) {
        return ingredientRecipes.stream()
                .map(IngredientRecipeDTO::getIngredientDTO)
                .collect(Collectors.toList());
    }

    private long findNextRecipeId() {
        Long maxId = recipeRepository.getMaxUrlId();
        if (maxId == null) {
            maxId = DEFAULT_URL_ID;
        }
        return ++maxId;
    }

    private void formatRecipeName(RecipeDTO recipeDTO) {
        String recipeName = recipeDTO.getRecipeName().trim();
        recipeName = recipeName.substring(0, 1).toUpperCase() + recipeName.substring(1);

        recipeDTO.setRecipeName(recipeName);
    }
}
