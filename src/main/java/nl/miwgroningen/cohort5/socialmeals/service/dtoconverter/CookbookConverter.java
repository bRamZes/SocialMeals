package nl.miwgroningen.cohort5.socialmeals.service.dtoconverter;

import nl.miwgroningen.cohort5.socialmeals.dto.CookbookDTO;
import nl.miwgroningen.cohort5.socialmeals.dto.RecipeDTO;
import nl.miwgroningen.cohort5.socialmeals.dto.SocialMealsUserDTO;
import nl.miwgroningen.cohort5.socialmeals.model.Cookbook;
import nl.miwgroningen.cohort5.socialmeals.model.SocialMealsUser;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Britt van Mourik
 * Converts Cookbooks to CookbookDTO's
 */

public class CookbookConverter {

    private final SocialMealsUserConverter socialMealsUserConverter;
    private final RecipeConverter recipeConverter;

    public CookbookConverter() {
        this.socialMealsUserConverter = new SocialMealsUserConverter();
        this.recipeConverter = new RecipeConverter();
    }

    public CookbookDTO toDTO(Cookbook cookbook) {

        SocialMealsUserDTO user = socialMealsUserConverter.toDTO(cookbook.getSocialMealsUser());
        List<RecipeDTO> recipeDTOs = recipeConverter.toListDTO(List.copyOf(cookbook.getRecipeLikes()));

        return new CookbookDTO(cookbook.getUrlId(), cookbook.getCookbookName(), user, recipeDTOs);
    }

    public List<CookbookDTO> toListDTO(List<Cookbook> cookbookList) {
        return cookbookList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Cookbook fromNewCookbookDTO(CookbookDTO cookbookDTO, SocialMealsUser socialMealsUser) {

        Cookbook cookbook = new Cookbook();
        cookbook.setCookbookName(cookbookDTO.getCookbookName());
        cookbook.setSocialMealsUser(socialMealsUser);
        cookbook.setRecipeLikes(new HashSet<>());
        cookbook.setUrlId(cookbookDTO.getUrlId());

        return cookbook;
    }

}
