package com.pfc.thindesk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchDTO {
    private String perfilAId;
    private String apelidoPerfilA;
    private String perfilBId;
    private String apelidoPerfilB;
    private boolean houveMatch;

}

