package pw.jonwinters.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnumResponse<T> {

    /**
     * name
     */
    private String name;

    /**
     * code
     */
    private T code;

}
