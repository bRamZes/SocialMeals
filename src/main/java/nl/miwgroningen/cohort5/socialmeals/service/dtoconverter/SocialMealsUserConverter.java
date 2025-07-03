package nl.miwgroningen.cohort5.socialmeals.service.dtoconverter;

import nl.miwgroningen.cohort5.socialmeals.dto.SocialMealsUserDTO;
import nl.miwgroningen.cohort5.socialmeals.model.SocialMealsUser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Wessel van Dommelen <w.r.van.dommelen@st.hanze.nl>
 *
 * Converts SocialMealsUsers into SocialMealsUsersDTO's and vice versa
 */
public class SocialMealsUserConverter {

    public List<SocialMealsUserDTO> toListSocialMealsUserDTOs(List<SocialMealsUser> socialMealsUsers) {
        return socialMealsUsers.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public SocialMealsUserDTO toDTO(SocialMealsUser socialMealsUser) {
        return new SocialMealsUserDTO(socialMealsUser.getUsername());
    }
}
