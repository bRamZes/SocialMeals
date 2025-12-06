package nl.miwgroningen.cohort5.socialmeals.controller;

import nl.miwgroningen.cohort5.socialmeals.dto.IngredientDTO;
import nl.miwgroningen.cohort5.socialmeals.dto.IngredientRecipeDTO;
import nl.miwgroningen.cohort5.socialmeals.dto.RecipeDTO;
import nl.miwgroningen.cohort5.socialmeals.dto.SocialMealsUserDTO;
import nl.miwgroningen.cohort5.socialmeals.model.IngredientRecipe;
import nl.miwgroningen.cohort5.socialmeals.service.IngredientService;
import nl.miwgroningen.cohort5.socialmeals.service.RecipeService;
import nl.miwgroningen.cohort5.socialmeals.service.implementation.SocialMealsUserDetailService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Lob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Wessel van Dommelen <w.r.van.dommelen@st.hanze.nl>
 *
 * controls the view of a new or update recipe.
 */

@Controller
@SessionAttributes("recipeStateKeeper")
public class RecipeCreateUpdateDeleteController {

    private final RecipeService recipeService;
    private final IngredientService ingredientService;
    private final SocialMealsUserDetailService socialMealsUserDetailService;

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeCreateUpdateDeleteController.class);

    public RecipeCreateUpdateDeleteController(RecipeService recipeService,
                                              IngredientService ingredientService,
                                              SocialMealsUserDetailService socialMealsUserDetailService) {
        this.recipeService = recipeService;
        this.ingredientService = ingredientService;
        this.socialMealsUserDetailService = socialMealsUserDetailService;
    }

    @ModelAttribute("recipeStateKeeper")
    public RecipeDTO recipeStateKeeper() {
        return new RecipeDTO();
    }

    @GetMapping("/recipe/new")
    protected String showRecipeForm(@ModelAttribute("recipeStateKeeper") RecipeDTO recipeStateKeeper,
                                    Model model) {

        RecipeDTO recipeDTO = new RecipeDTO();
        setRecipeKeeperValuesWithRecipeDTOValues(recipeStateKeeper, recipeDTO);
        recipeStateKeeper.setUrlId(recipeDTO.getUrlId());
        recipeStateKeeper.getSteps().add("");

        model.addAttribute("recipeDTO", recipeStateKeeper);
        return "recipeForm";
    }


    @PostMapping(value = "/recipe/new/newRecipe", params = "add")
    protected String updateShowRecipeForm(Model model,
                                          @ModelAttribute("recipeDTO") RecipeDTO recipeDTO,
                                          @SessionAttribute("recipeStateKeeper") RecipeDTO recipeStateKeeper,
                                          BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/";
        }
        setRecipeKeeperValuesWithRecipeDTOValues(recipeStateKeeper, recipeDTO);
        recipeStateKeeper.getSteps().add("");

        model.addAttribute("recipeDTO", recipeStateKeeper);
        return "recipeForm";
    }


    @PostMapping(value = "/recipe/new/newRecipe", params = "save")
    protected String saveRecipe(Model model,
                                @ModelAttribute("recipeDTO") RecipeDTO recipeDTO,
                                Principal principal,
                                BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/";
        }

        setRecipeSocialMealsUser(recipeDTO, principal.getName());
        recipeDTO.setSteps(removeEmptySteps(recipeDTO.getSteps()));
        recipeService.addNew(recipeDTO);

        return "redirect:/recipe/update/" + recipeDTO.getUrlId();
    }

    @GetMapping("/recipe/update/{urlId}")
    protected String showUpdateRecipe(@PathVariable("urlId") Long urlId,
                                      @ModelAttribute("recipeStateKeeper") RecipeDTO recipeStateKeeper,
                                      Model model,
                                      Principal principal) {

        RecipeDTO existingRecipe = recipeService.findByUrlId(urlId);
        setRecipeKeeperValuesWithRecipeDTOValues(recipeStateKeeper, existingRecipe);

        if (recipeUserDoesNotMatchCurrentUser(principal, recipeStateKeeper)) {
            return "redirect:/MyKitchen";
        }
        refreshUpdateRecipe(recipeStateKeeper, model);
        model.addAttribute("recipeImage", Base64.encodeBase64String(existingRecipe.getRecipeImage()));

        return "updateRecipeForm";
    }

    @Lob
    @PostMapping(value = "/recipe/update/{urlId}/updateImage")
    protected String updateRecipeWithImage(@ModelAttribute("recipeStateKeeper") RecipeDTO recipeDTO,
                                           @PathVariable("urlId") Long urlId,
                                           @RequestParam MultipartFile multipartImage,
                                           BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/";
        }

        try {
            recipeDTO.setRecipeImage(multipartImage.getBytes());
        } catch (IOException exception) {
            LOGGER.error("Unable to process uploaded image", exception);
        }

        recipeService.updateRecipeWithImage(recipeService.findByUrlId(urlId), recipeDTO);

        return "redirect:/recipe/update/" + urlId;
    }

    @PostMapping(value = "/recipe/update/updateSteps", params = "add")
    protected String addStepToUpdateRecipe(@ModelAttribute("recipeDTO") RecipeDTO recipeDTO,
                                           @SessionAttribute("recipeStateKeeper") RecipeDTO recipeStateKeeper,
                                           Model model,
                                           BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/MyKitchen";
        }

        setRecipeKeeperValuesWithRecipeDTOValues(recipeStateKeeper, recipeDTO);
        recipeStateKeeper.getSteps().add("");

        refreshUpdateRecipe(recipeStateKeeper, model);
        return "updateRecipeForm";
    }

    @PostMapping(value = "/recipe/update/updateSteps", params = "save")
    protected String updateRecipe(@ModelAttribute("recipeDTO") RecipeDTO recipeDTO,
                                  @SessionAttribute("recipeStateKeeper") RecipeDTO recipeStateKeeper,
                                  Model model,
                                  BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/MyKitchen";
        }

        recipeDTO.setSteps(removeEmptySteps(recipeDTO.getSteps()));
        RecipeDTO oldRecipe = recipeService.findByUrlId(recipeStateKeeper.getUrlId());
        recipeService.updateRecipe(oldRecipe, recipeDTO);

        return "redirect:/recipe/update/" + recipeStateKeeper.getUrlId();
    }

    @GetMapping(value = "/recipe/deleteStep/{iterIndex}")
    protected String deleteStepRecipe(@PathVariable("iterIndex") int iterIndex,
                                      @SessionAttribute("recipeStateKeeper") RecipeDTO recipeStateKeeper,
                                      Model model) {

        recipeStateKeeper.getSteps().remove(iterIndex);
        RecipeDTO existingRecipe = recipeService.findByUrlId(recipeStateKeeper.getUrlId());

        if (existingRecipe == null) {
            model.addAttribute("recipeDTO", recipeStateKeeper);
            return "recipeForm";
        }

        existingRecipe.setSteps(recipeStateKeeper.getSteps());
        refreshUpdateRecipe(existingRecipe, model);
        return "updateRecipeForm";
    }

    @PostMapping(value = "/recipe/update/{urlId}/addingredient")
    protected String addIngredient(@ModelAttribute("ingredientRecipeDTO") IngredientRecipeDTO ingredientRecipeDTO,
                                   @PathVariable("urlId") Long urlId,
                                   @RequestParam("ingredientName") String ingredientName,
                                   BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/";
        }

        IngredientDTO ingredientDTO = getMatchingOrNewIngredientDTO(ingredientName);
        ingredientRecipeDTO.setRecipeDTO(recipeService.findByUrlId(urlId));
        ingredientRecipeDTO.setIngredientDTO(ingredientDTO);

        try {
            recipeService.addIngredientToRecipe(ingredientRecipeDTO);
        } catch (NullPointerException error) {
            LOGGER.error("Failed to add ingredient to recipe", error);
        }

        return "redirect:/recipe/update/" + urlId;
    }

    @GetMapping("/recipe/delete/{urlId}/{ingredientName}")
    protected String deleteIngredient(@PathVariable("urlId") Long urlId,
                                      @PathVariable("ingredientName") String ingredientName) {

        try {
            IngredientRecipe ingredientRecipe = recipeService.getIngredientRecipeByNameAndUrlId(ingredientName, urlId);
            recipeService.deleteIngredientFromRecipe(ingredientRecipe);
        } catch (NullPointerException error) {
            LOGGER.error("Failed to delete ingredient from recipe", error);
        }

        return "redirect:/recipe/update/" + urlId;
    }

    @RequestMapping(value = "/recipe/update/ingredientAutocomplete")
    @ResponseBody
    public List<String> ingredientAutocomplete(@RequestParam(value = "term") String keyword,
                                               @RequestParam(value = "urlId") Long urlId) {
        List<String> searchIngredients = ingredientService.search(keyword);
        List<String> remainingIngredients = recipeService.getRemainingIngredientsByUrlId(urlId)
                .stream().map(IngredientDTO::getIngredientName).collect(Collectors.toList());
        return searchIngredients.stream().filter(remainingIngredients::contains).collect(Collectors.toList());
    }

    private void setRecipeKeeperValuesWithRecipeDTOValues(RecipeDTO recipeStateKeeper, RecipeDTO recipeDTO) {
        recipeStateKeeper.setRecipeName(recipeDTO.getRecipeName());
        recipeStateKeeper.setSteps(recipeDTO.getSteps());
        recipeStateKeeper.setSocialMealsUserDTO(recipeDTO.getSocialMealsUserDTO());

    }

    private List<String> removeEmptySteps(List<String> steps) {
        return steps.stream().filter(i -> !i.isEmpty()).collect(Collectors.toList());
    }

    private void refreshUpdateRecipe(RecipeDTO recipeDTO, Model model) {

        model.addAttribute("recipeDTO", recipeDTO);
        model.addAttribute("ingredientRecipeDTO", new IngredientRecipeDTO());
        model.addAttribute("presentIngredientsRecipes", recipeService.getIngredientRecipesByRecipeUrlId(recipeDTO.getUrlId()));
        model.addAttribute("remainingIngredients", recipeService.getRemainingIngredientsByUrlId(recipeDTO.getUrlId()));
    }

    private boolean recipeUserDoesNotMatchCurrentUser(Principal principal, RecipeDTO recipeDTO) {
        SocialMealsUserDTO currentUser = socialMealsUserDetailService.getUserByUsername(principal.getName());
        return !currentUser.equals(recipeDTO.getSocialMealsUserDTO());
    }

    private void setRecipeSocialMealsUser(RecipeDTO recipeDTO, String name) {
        SocialMealsUserDTO socialMealsUserDTO = socialMealsUserDetailService.getUserByUsername(name);
        if (socialMealsUserDTO != null) {
            recipeDTO.setSocialMealsUserDTO(socialMealsUserDTO);
        }
    }

    private IngredientDTO getMatchingOrNewIngredientDTO(String ingredientName) {
        IngredientDTO ingredientDTO = ingredientService.findByIngredientName(ingredientName);
        if (ingredientDTO == null) {
            ingredientDTO = ingredientService.addNew(new IngredientDTO(ingredientName));
        }
        return ingredientDTO;
    }
}
