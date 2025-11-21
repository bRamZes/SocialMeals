package nl.miwgroningen.cohort5.socialmeals.comparator;

import nl.miwgroningen.cohort5.socialmeals.dto.RecipeDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeDTOComparatorTest {

    @Test
    void sortAscendingByName() {
        List<RecipeDTO> recipes = new ArrayList<>();
        recipes.add(new RecipeDTO("B", new ArrayList<>(), null));
        recipes.add(new RecipeDTO("A", new ArrayList<>(), null));
        recipes.sort(new RecipeDTOAscComparator());
        assertThat(recipes.get(0).getRecipeName()).isEqualTo("A");
    }

    @Test
    void sortDescendingByName() {
        List<RecipeDTO> recipes = new ArrayList<>();
        recipes.add(new RecipeDTO("A", new ArrayList<>(), null));
        recipes.add(new RecipeDTO("B", new ArrayList<>(), null));
        recipes.sort(new RecipeDTODescComparator());
        assertThat(recipes.get(0).getRecipeName()).isEqualTo("B");
    }
}
