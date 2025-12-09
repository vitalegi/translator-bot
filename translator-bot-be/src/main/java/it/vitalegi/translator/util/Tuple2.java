package it.vitalegi.translator.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Tuple2<T1, T2> {
    T1 t1;
    T2 t2;
}
