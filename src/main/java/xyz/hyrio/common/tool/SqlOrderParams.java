package xyz.hyrio.common.tool;

import xyz.hyrio.common.exception.request.InvalidParameterException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;
import static xyz.hyrio.common.util.ObjectUtils.WHITESPACE_REGEX;

/**
 * The sorting criteria used when getting the list of entities.
 *
 * @author Hyrio 2022/05/10
 */
public record SqlOrderParams(String field, boolean descending) {
    public static final char FIELD_SEPARATOR_CHAR = ':';
    public static final String FIELD_SEPARATOR_STRING = String.valueOf(FIELD_SEPARATOR_CHAR);

    /**
     * Convert camel case nomenclature to underscore nomenclature.
     *
     * @param camelCase Variable name in camel case nomenclature.
     * @return Variable name in underscore nomenclature.
     */
    private static String camelCase2Underscore(String camelCase) {
        if (!hasText(camelCase)) {
            throw new IllegalArgumentException("input field name cannot be empty");
        }
        if (!camelCase.matches("[a-z]+\\d*([A-Z][a-z\\d]*)*\\d*")) {
            throw new IllegalArgumentException("input field name is not camel case nomenclature: " + camelCase);
        }
        String[] parts = camelCase.split("(?<!^)(?=[A-Z])");
        StringBuilder ret = new StringBuilder();
        for (String part : parts) {
            ret.append(part.toLowerCase());
            ret.append("_");
        }
        return ret.substring(0, ret.length() - 1);
    }

    /**
     * Convert the incoming sort string into a parameter list.
     *
     * @param orderParams     The sorting string passed in, such as {@code name-,age+}.
     * @param availableFields Optional field names, passed in as a variable number of strings.
     *                        The default form is to specify the Java field name,
     *                        and the database field name is the underscore form of the Java field name.
     *                        (the return value of {@link #camelCase2Underscore(String)})
     *                        If you want to specify the database field name,
     *                        use {@code :} to separate the Java field name and the database field name,
     *                        for example, {@code "name"}, {@code "moduleId:c.module_id"}.
     * @return The sorted parameter list after conversion.
     */
    public static List<SqlOrderParams> parseOrderParams(String orderParams, String... availableFields) {
        if (isEmpty(availableFields)) {
            throw new IllegalArgumentException("available field list is empty");
        }
        if (!hasText(orderParams)) {
            return null;
        }

        Map<String, String> availableFieldsMap = new HashMap<>(availableFields.length); // javaField, databaseField
        for (String field : availableFields) {
            if (!hasText(field)) {
                throw new IllegalArgumentException("available field is null or empty");
            }
            if (field.contains(FIELD_SEPARATOR_STRING)) {
                int pos = field.lastIndexOf(FIELD_SEPARATOR_CHAR);
                if (pos == 0 || pos == field.length() - 1) {
                    throw new IllegalArgumentException("invalid available field: " + field);
                }
                availableFieldsMap.put(field.substring(0, pos), field.substring(pos + 1));
            } else {
                availableFieldsMap.put(field, camelCase2Underscore(field));
            }
        }
        String[] orderParamArray = orderParams.replaceAll(WHITESPACE_REGEX, "").split(",");
        List<SqlOrderParams> ret = new LinkedList<>();
        for (String orderParam : orderParamArray) {
            if (!hasText(orderParam)) {
                continue;
            }
            String field;
            boolean descending;
            if (orderParam.endsWith("-")) {
                field = orderParam.substring(0, orderParam.length() - 1);
                descending = true;
            } else if (orderParam.endsWith("+")) {
                field = orderParam.substring(0, orderParam.length() - 1);
                descending = false;
            } else {
                field = orderParam;
                descending = false;
            }
            if (availableFieldsMap.containsKey(field)) {
                ret.add(new SqlOrderParams(availableFieldsMap.get(field), descending));
            } else {
                throw new InvalidParameterException("invalid order field: " + field);
            }
        }
        return isEmpty(ret) ? null : ret;
    }
}
