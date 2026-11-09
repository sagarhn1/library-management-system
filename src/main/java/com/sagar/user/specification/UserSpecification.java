package com.sagar.user.specification;

import com.sagar.user.model.Role;
import com.sagar.user.model.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * Provides reusable JPA Specifications for dynamically filtering users.
 *
 * <p>
 * Each specification represents an optional filter that can be combined
 * with other specifications to build dynamic database queries.
 * </p>
 */
public class UserSpecification {

    /**
     * Utility class; prevent instantiation.
     */
    private UserSpecification() {
    }

    /**
     * Filters users by their assigned role.
     *
     * @param role role to filter by
     * @return specification for the role filter, or {@code null}
     *         when no role is provided
     */
    public static Specification<User> hasRole(Role role) {
        return (root, query, criteriaBuilder) ->
                role == null
                        ? null
                        : criteriaBuilder.equal(root.get("role"), role);
    }

    /**
     * Filters users based on their active status.
     *
     * @param active active status to filter by
     * @return specification for the active-status filter, or {@code null}
     *         when no status is provided
     */
    public static Specification<User> isActive(Boolean active) {
        return (root, query, criteriaBuilder) ->
                active == null
                        ? null
                        : criteriaBuilder.equal(root.get("active"), active);
    }

    /**
     * Performs a case-insensitive partial search on the user's first name.
     *
     * <p>
     * For example, searching for {@code "sag"} can match
     * names such as {@code "Sagar"}.
     * </p>
     *
     * @param firstName first name search value
     * @return specification for the first-name search,
     *         or {@code null} when the value is empty
     */
    public static Specification<User> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) ->
                firstName == null || firstName.isBlank()
                        ? null
                        : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        "%" + firstName.toLowerCase() + "%"
                );
    }

    /**
     * Performs a case-insensitive partial search on the user's last name.
     *
     * @param lastName last name search value
     * @return specification for the last-name search,
     *         or {@code null} when the value is empty
     */
    public static Specification<User> hasLastName(String lastName) {
        return (root, query, criteriaBuilder) ->
                lastName == null || lastName.isBlank()
                        ? null
                        : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        "%" + lastName.toLowerCase() + "%"
                );
    }

    /**
     * Performs a case-insensitive partial search on the user's email.
     *
     * @param email email search value
     * @return specification for the email search,
     *         or {@code null} when the value is empty
     */
    public static Specification<User> hasEmail(String email) {
        return (root, query, criteriaBuilder) ->
                email == null || email.isBlank()
                        ? null
                        : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%"
                );
    }
}