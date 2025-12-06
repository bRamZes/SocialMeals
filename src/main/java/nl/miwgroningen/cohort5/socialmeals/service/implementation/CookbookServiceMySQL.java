package nl.miwgroningen.cohort5.socialmeals.service.implementation;

import nl.miwgroningen.cohort5.socialmeals.dto.CookbookDTO;
import nl.miwgroningen.cohort5.socialmeals.dto.RecipeDTO;
import nl.miwgroningen.cohort5.socialmeals.dto.SocialMealsUserDTO;
import nl.miwgroningen.cohort5.socialmeals.model.Cookbook;
import nl.miwgroningen.cohort5.socialmeals.model.Recipe;
import nl.miwgroningen.cohort5.socialmeals.model.SocialMealsUser;
import nl.miwgroningen.cohort5.socialmeals.repository.CookbookRepository;
import nl.miwgroningen.cohort5.socialmeals.service.CookbookService;
import nl.miwgroningen.cohort5.socialmeals.service.RecipeService;
import nl.miwgroningen.cohort5.socialmeals.service.dtoconverter.CookbookConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Britt van Mourik
 * Collects and stores Cookbooks in the MySQL Database
 */

@Service
public class CookbookServiceMySQL implements CookbookService {
    private static final long DEFAULT_URL_ID = 5000;

    private final CookbookRepository cookbookRepository;

    private final SocialMealsUserDetailService socialMealsUserDetailService;
    private final RecipeService recipeService;

    private final CookbookConverter cookbookConverter;

    @Autowired
    public CookbookServiceMySQL(CookbookRepository cookbookRepository,
                                SocialMealsUserDetailService socialMealsUserDetailService,
                                RecipeService recipeService) {
        this.cookbookRepository = cookbookRepository;
        this.socialMealsUserDetailService = socialMealsUserDetailService;
        this.recipeService = recipeService;

        cookbookConverter = new CookbookConverter();
    }

    @Override
    public CookbookDTO findByUrlId(Long urlId) {
        Optional<Cookbook> cookbook = cookbookRepository.findByUrlId(urlId);
        if (cookbook.isEmpty()) {
            return null;
        }
        return cookbookConverter.toDTO(cookbook.get());
    }

    @Override
    public List<CookbookDTO> getCookbooksByUser(SocialMealsUserDTO socialMealsUserDTO) {
        SocialMealsUser socialMealsUser = socialMealsUserDetailService.getUserByDTO(socialMealsUserDTO);

        List<Cookbook> cookbookList = cookbookRepository.findCookbooksBySocialMealsUser(socialMealsUser);

        return cookbookConverter.toListDTO(cookbookList);
    }

    @Override
    public CookbookDTO addNew(CookbookDTO cookbookDTO) {
        cookbookDTO.setUrlId(findNextCookbookId());
        formatCookbookName(cookbookDTO);

        SocialMealsUser socialMealsUser = socialMealsUserDetailService.getUserByDTO(cookbookDTO.getSocialMealsUser());
        cookbookRepository.save(cookbookConverter.fromNewCookbookDTO(cookbookDTO, socialMealsUser));
        return cookbookDTO;
    }

    @Override
    public CookbookDTO updateCookbook(CookbookDTO cookbookDTO) {
        Optional<Cookbook> optionalCookbook = cookbookRepository.findByUrlId(cookbookDTO.getUrlId());
        if (optionalCookbook.isEmpty()) {
            return null;
        }
        Cookbook cookbook = optionalCookbook.get();

        formatCookbookName(cookbookDTO);
        cookbook.setCookbookName(cookbookDTO.getCookbookName());
        cookbookRepository.save(cookbook);

        return cookbookDTO;
    }

    @Override
    public CookbookDTO deleteCookbook(CookbookDTO cookbookDTO) {
        Optional<Cookbook> optionalCookbook = cookbookRepository.findByUrlId(cookbookDTO.getUrlId());
        optionalCookbook.ifPresent(cookbookRepository::delete);
        return cookbookDTO;
    }

    @Override
    public CookbookDTO addRecipeDTO(CookbookDTO cookbookDTO, RecipeDTO recipeDTO) {
        Optional<Cookbook> cookbook = cookbookRepository.findByUrlId(cookbookDTO.getUrlId());
        if (cookbook.isEmpty()) {
            return null;
        }

        Cookbook actualCookbook = cookbook.get();
        Recipe actualRecipe = recipeService.getRecipeByRecipeDTO(recipeDTO);

        actualCookbook.getRecipeLikes().add(actualRecipe);
        cookbookRepository.save(actualCookbook);
        return cookbookConverter.toDTO(actualCookbook);
    }

    @Override
    public CookbookDTO removeRecipeDTO(CookbookDTO cookbookDTO, RecipeDTO recipeDTO) {
        Optional<Cookbook> cookbook = cookbookRepository.findByUrlId(cookbookDTO.getUrlId());
        if (cookbook.isEmpty()) {
            return null;
        }

        Cookbook actualCookbook = cookbook.get();
        Recipe actualRecipe = recipeService.getRecipeByRecipeDTO(recipeDTO);

        actualCookbook.getRecipeLikes().remove(actualRecipe);
        cookbookRepository.save(actualCookbook);
        return cookbookConverter.toDTO(actualCookbook);
    }

    @Override
    public List<RecipeDTO> searchInCookbook(CookbookDTO cookbookDTO, String keyword) {
        List<Long> searchList = recipeService.search(keyword);

        List<Long> filteredByCookbook = cookbookDTO.getRecipes()
                .stream()
                .map(RecipeDTO::getUrlId)
                .filter(searchList::contains)
                .collect(Collectors.toList());

        return filteredByCookbook.stream()
                .map(recipeService::findByUrlId)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeDTO> searchInCookbook(SocialMealsUserDTO socialMealsUserDTO, String keyword) {
        List<Long> searchList = recipeService.search(keyword);

        List<Long> filteredByCookbook = recipeService.getRecipesByUsername(socialMealsUserDTO.getUsername())
                .stream()
                .map(RecipeDTO::getUrlId)
                .filter(searchList::contains)
                .collect(Collectors.toList());

        return filteredByCookbook.stream()
                .map(recipeService::findByUrlId)
                .collect(Collectors.toList());
    }

    private long findNextCookbookId() {
        Long maxId = cookbookRepository.getMaxUrlId();
        if (maxId == null) {
            maxId = DEFAULT_URL_ID;
        }
        return ++maxId;
    }

    private void formatCookbookName(CookbookDTO cookbookDTO) {
        String cookbookName = cookbookDTO.getCookbookName().trim();
        cookbookName = cookbookName.substring(0, 1).toUpperCase() + cookbookName.substring(1);

        cookbookDTO.setCookbookName(cookbookName);
    }

}
