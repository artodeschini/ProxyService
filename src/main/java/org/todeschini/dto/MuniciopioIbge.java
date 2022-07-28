package org.todeschini.dto;

//import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.todeschini.utils.StringUtils;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MuniciopioIbge implements Serializable {

    private String nome;
    private String normalize;
    private String codigo;
    private String uf;

    public void setNormalize(String normalize) {
        this.normalize = StringUtils.normalize(normalize);
    }
}
