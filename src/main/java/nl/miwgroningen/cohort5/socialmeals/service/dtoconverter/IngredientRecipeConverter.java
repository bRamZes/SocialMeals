package nl.miwgroningen.cohort5.socialmeals.service.dtoconverter;

import nl.miwgroningen.cohort5.socialmeals.dto.IngredientRecipeDTO;
import nl.miwgroningen.cohort5.socialmeals.model.Ingredient;
import nl.miwgroningen.cohort5.socialmeals.model.IngredientRecipe;
import nl.miwgroningen.cohort5.socialmeals.model.Recipe;
import nl.miwgroningen.cohort5.socialmeals.service.IngredientService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Britt van Mourik
 *
 * Converts IngredientRecipes into IngredientRecipeDTO's and vice versa
 */

public class IngredientRecipeConverter {

    private final IngredientService ingredientService;

    private final RecipeConverter recipeConverter;
    private final IngredientConverter ingredientConverter;

    public IngredientRecipeConverter(IngredientService ingredientService) {
        this.ingredientService = ingredientService;

        ingredientConverter = new IngredientConverter();
        recipeConverter = new RecipeConverter();
    }

    public IngredientRecipe fromDTO(IngredientRecipeDTO ingredientRecipeDTO, Recipe recipe) {

        Ingredient ingredient = ingredientService.getIngredientByIngredientDTO(ingredientRecipeDTO.getIngredientDTO());

        if (ingredient == null) {
            return null;
        }

        return new IngredientRecipe(
                ingredient,
                recipe,
                ingredientRecipeDTO.getQuantity(),
                ingredientRecipeDTO.getQuantityType()
        );
    }

    public IngredientRecipeDTO toDTO(IngredientRecipe ingredientRecipe) {

        return new IngredientRecipeDTO(
                ingredientConverter.toDTO(ingredientRecipe.getIngredient()),
                recipeConverter.toDTO(ingredientRecipe.getRecipe()),
                ingredientRecipe.getQuantity(),
                ingredientRecipe.getQuantityType());

    }

    public List<IngredientRecipeDTO> toListDTO(List<IngredientRecipe> ingredientRecipeList) {
        return ingredientRecipeList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
