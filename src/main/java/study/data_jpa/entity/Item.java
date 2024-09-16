package study.data_jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Item {

    @Id @GeneratedValue
    private Long id; // Wrapper 타입이어야 null값 가능
}
