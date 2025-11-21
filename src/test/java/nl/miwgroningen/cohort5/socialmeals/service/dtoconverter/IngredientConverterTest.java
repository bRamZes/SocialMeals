package nl.miwgroningen.cohort5.socialmeals.service.dtoconverter;

import nl.miwgroningen.cohort5.socialmeals.dto.IngredientDTO;
import nl.miwgroningen.cohort5.socialmeals.model.Ingredient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IngredientConverterTest {

    private final IngredientConverter converter = new IngredientConverter();

    @Test
    void toDTOConvertsName() {
        Ingredient ingredient = new Ingredient("Sugar");
        IngredientDTO dto = converter.toDTO(ingredient);
        assertThat(dto.getIngredientName()).isEqualTo("Sugar");
    }

    @Test
    void fromDTOCreatesIngredient() {
        IngredientDTO dto = new IngredientDTO("Salt");
        Ingredient ingredient = converter.fromDTO(dto);
        assertThat(ingredient.getIngredientName()).isEqualTo("Salt");
    }
}
