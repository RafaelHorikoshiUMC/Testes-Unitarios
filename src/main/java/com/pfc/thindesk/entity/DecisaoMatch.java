package com.pfc.thindesk.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "decisoes")
@Data
@NoArgsConstructor
@AllArgsConstructor

@CompoundIndexes({
        @CompoundIndex(name = "unico_match", def = "{'perfilOrigemId' : 1, 'perfilAlvoId': 1}", unique = true)
})

public class DecisaoMatch {
    private String id;
    private String perfilOrigemId;
    private String perfilAlvoId;
    private boolean deuMatch;
    // outros campos...
}
