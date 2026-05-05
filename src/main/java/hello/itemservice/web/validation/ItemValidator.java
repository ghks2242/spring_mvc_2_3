package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
        // isAssignableFrom 사용이유 (자식클래스까지 커버가 가능하다)
//        -> item == clazz
//        -> item == subItem
    }

    @Override
    public void validate(Object target, Errors errors) {
        Item item = (Item) target;

        // 검증 오류 결과를 보관.
//        Map<String, String> errors = new HashMap<>();
        // -> errors 역할을 bindingResult 가 수행한다

        // 이거와 아래에 로직이 같다 해당하는 유틸은 empty 와 공백같은 단순한기능만제공
//        ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");

        // 검증로직
        if (!StringUtils.hasText(item.getItemName())) {
//            errors.put("itemName", "상품 이름은 필수입니다.");
//            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),false, new String[]{"required.item.itemName"}, null, null));
            errors.rejectValue("itemName", "required");
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
//            errors.put("price", "가격은 1,000 ~ 1,000,000 까지 허용합니다.");
//            errors.addError(new FieldError("item", "price", item.getPrice(), false, new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
            errors.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
//            errors.put("quantity", "수량은 최대 9,999 까지 허용합니다.");
//            errors.addError(new FieldError("item", "quantity", item.getQuantity(), false, new String[]{"max.item.quantity"}, new Object[]{9999}, null));
            errors.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        // 특정필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
//                errors.put("globalError", "가격 * 수량의 합은 10,000원 이상이어야합니다. 현재 값 = " + resultPrice);
//                errors.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000,resultPrice}, null));
                errors.reject("totalPriceMin", new Object[]{10000,resultPrice}, null);
            }
        }
    }

}
