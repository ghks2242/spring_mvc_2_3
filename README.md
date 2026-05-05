검증
---
클라이언트 검증과 서버검증
* 클라이언트 검증은 조작할수있으므로 보안에 취약하다
* 서버만으로 검증하면, 즉각적인 고객사용성이 부족하다
* 둘을적절히 섞어서 사용하되, 최종적으로 서버검증은필수이다.
* API 방식을 사용하면 API스펙을 잘 정의해서 검증 오류를 API 응답결과에 잘남겨주어야한다

# 스프링에서 제공하는 검증오류 처리방법 

## BindingResult
* BindingResult 파라미터위치는 @ModelAttribute 뒤에와야한다
  * (ex) public String addItemV1(```@ModelAttribute Item item, BindingResult bindingResult```, RedirectAttributes redirectAttributes, Model model) { ...
* 필드오류는 FieldError 에 담는다
  * public FieldError(String objectName, String field, String defaultMessage) 
* 글로벌오류는 ObjectError 에 담는다
  * public ObjectError(String objectName, String defaultMessage) 

### 타임리프 스프링 검증 오류 통합 기능
타임리프는 스프링의 BindingResult 를 활용해서 편리하게 검증 오류를 표현하는 기능을 제공한다.
* #fields : #fields 로 BindingResult 가 제공하는 검증오류에 접근할수있다.
* th:errors : 해당필드에 오류가있는경우에 태그를 출력한다 (th:if) 편의버전
* th:errorclass : th:field 에서 지정한 필드오류가있으면 class 정보를 추가한다


## BindingResult2
* 스프링이 제공하는 검증 오류를 보관하는 객체이다. 검증 오류가 발생하면 여기에 보관하면된다.
* BindingResult 가 있으면 @ModelAttribute 에 데이터 바인딩시 오류가 발생해도 컨트롤러가 호출된다.
  * ModelAttribute 에 바인딩시 타입오류가발생하면
    * BindingResult 가 없으면 -> 400 오류가 발생하면서 컨트롤러가 호출되지않고 오류페이지로이동한다
    * BindingResult 가 있으면 -> 오류정보를 (FieldError) BindingResult 에 담아서 컨트롤러를 정상호출한다.

### BindingResult 에 검증 오류를 적용하는 3가지방법
* ModelAttribute 의 객체에 타입오류등으로 바인딩이 실패하는 경우 스프링이 FieldError 생성해 BindingResult 넣어준다
* 개발자가 직접 넣어준다
* Validator 를 사용한다


### 주의 
* BindingResult 는 검증할 대상 바로 다음에와야한다!!!
* BindingResult 는 모델에 자동으로 포함된다

### BindingResult 와 Errors
```
BindingResult 는 인터페이스고 Errors 인터페이스를 상속받고있다.
실제 넘어오는 구현체는 BeanProertyBindingResult 라는 것인데, 둘다 구현하고있으므로 BindingResult 대신에 Errors를 사용해도된다.
Errors 인터페이스는 단순한 오류저장과 조회 기능을 제공한다 BindingResult 는 여기에 더해서 추가적인 기능들을 제공한다.
addError() 도 BindingResult 가 제공하므로 여기서는 BindingResult 를 사용하자 주로 관례상 BindingResult 를 많이 사용한다.
```

### FieldError, ObjectError

FieldError 의 생성자
```java
public FieldError(String objectName, String field, String defaultMessage)

public FieldError(String objectName, String field, @Nullable Object rejectedValue, boolean bindingFailure,
                  @Nullable String[] codes, @Nullable Object[] arguments, @Nullable String defaultMessage)
 
```

파라미터 목록 
* objectName : 오류가 발생한 객체이름
* field : 오류 필드
* rejectedValue : 사용자가 입력한 값(거절된값) 
* bindingFailure : 타입오류같은 바인딩실패인지, 검증실패인지 구분값
* codes : 메시지코드
* arguments : 메시지에서 사용하는인자
* defaultMessage : 기본오류메시지

ObjectError 도 유사하게 두가지 생성자를 제공한다

### 오류발생시 사용자입력값 유지
사용자의 입력 데이터가 컨트롤러의 @ModelAttribute 에 바인딩되는 시점에 오류가 발생하면 모델 객체에 사용자 입력 값을 유지하기 어렵다.
예를들어서 가격에 숫자가 아닌 문자가 입력된다면 가격은 Integer 타입이므로 문자를 보관할수있는 방법이 없다
그래서 오류가 발생한 경우 사용자 입력값을 보관하는 별도의 방법이필요하다 이렇게보관한 사용자 입력값을 검증오류 발생시 화면에다시 출력하면된다.


### 타임리프의 사용자 입력값 유지
```
th:field="*{price}"
타임리프의 th:field 는 매우 똑똑하게 동작한다, 정상상황에는 모델객체의 값을 사용하지만 오류가발생하면 FieldError 에서 보관한값을 사용해서 값을 출력한다.
```

### 스프링의 바인딩 오류처리
타입에러 발생시 스프링에서 컨트롤러 타기전에 미리 FieldError 생성하여 BindingResult 넣어준다

eorros.properties 를 사용하면 메시지 국제화처럼 사용할수도있다 
---

### 오류코드 메시지처리
FieldError , ObjectError 는 다루기가 너무번거롭다
오류코드를 좀더 자동화 할수있지않을까?

컨트롤러에서 BindingResult 는 검증헤야 할 객체인 target 바로 다음에 온다, 따라서 BindingResult는 이미 본인이 검증해야할 객체인 target을 알고있다

```java
log.info("objectName={}", bindingResult.getObjectName());
log.info("target={}", bindingResult.getTarget());
```
출력결과
```
objectName=item 
target=Item(id=null, itemName=, price=null, quantity=null)
```

### rejcetValue(), reject()
BindingResult 가 제공하는 rejectValue(), reject() 를 사용하면 FieldError, ObjectError 를 직접 생성하지 않고 깔끔하게 오류를 다룰수있다.

### rejectValue()
```java
void rejectValue(@Nullable String field, String errorCode,
			@Nullable Object[] errorArgs, @Nullable String defaultMessage);
```

* field : 오류 필드명
* errorCode : 오류 코드 (이 오류코드는 메시지에 등록된 코드가아니다. messageResolver를 위한 오류코드이다)
* errorArgs : 오류 메시지에서 {0} 을 치환하기 위한 값
* defaultMessage : 오류 메시지를 찾을수없을때 사용하는 기본메시지

### 축약된 오류코드
FieldError() 를 직접 다룰때에는 range.item.price 와 같이 모두 입력하였다 
그런데 rejectValue() 를 사용하고부터는 오류코드를 range 로 간단하게 입력했다. 그래도 오류메시지를 잘 찾아서 출력한다.
무언가 규칙이 있는것처럼 보인다. 이 부분을 이해하려면 MessageCodesResolver 를 이해해야한다. 왜 이런식으로 오류 코드를 구성하는지 바로 다음에 자세히 알아보자

````
오류코드를 만들때 다음과같이 자세히 만들수도있고 
required.item.itemName : 상품이름은 필수입니다
range.item.price : 상품의 가격 범위 오류입니다.

또는 다음과같이 단순하게 만들수도있다
required : 필수 값 입니다.
range : 범위 오류입니다.
```` 
단순하게 만들면 범용성이 좋아서 여러곳에서 사용할수있지만, 메시지를 세밀하게 작성하기어렵다.
반대로 자세하게만들면 범용성이 떨어진다. 가장 좋은방법은 범용성으로 사용하다가 세밀하게 작성해야하는 경우 세밀한내용이 적용되도록 메시지에 단계를 두는것이다

``` 
예를들어서 ```required``` 라고 오류코드가 있으면 이 해당하는 코드를 사용하는것이다
그런데 오류메시지에 ```required.item.itemName``` 와 같이 객체명과 필드명을 조합한 세밀한 메시지코드가있으면 이메시지를 우선순위로 사용하는것이다.
```
물론 이렇게 추가 개발을 해야겠지만 스프링의 MessageCodesResolver 라는 것으로 이러한 기능을 지원한

### MessageCodesResolver
* 검증 오류 코드로 메시지 코드들을 생성한다
* MessageCodesResolver 는 인터페이스이고 DefaultMessageCodesResolver 는 기본 구현체이다.
* 주로 다음과 함께 사용 ObjectError, FiledError (BindingResult 의 rejectValue, reject 사용시 내부에서 FiledError , ObjectError 를 사용한다)

### DefaultMessageCodesResolver 기본메시지 생성 규칙
MessageCodesResolverTest 참고

객체오류
```
객체 오류의 경우 다음 순서로 2가지 생성
1. : code + "." + object name
2. : code
```

필드오류
```
필드 오류의 경우 다음 순서로 4가지 생성
1. : code + "." + object name + "." + field
2. : code + "." + field
3. : code + "." + field type
4. : code
```

## 핵심은 구체적인것에서 덜 구체적인 것으로
MessageCodesResolver 는 required.item.itemName 처럼 구체적인 것을 먼저 만들어주고 required 처럼 덜구체적인 것을 가장나중에만든다.
이렇게 하면 앞서말한것처럼 메시지와 관련된 공통전략을 편리하게 도입할수있다.

# 정리
* rejectValue() 호출
* MessageCodesResolver 를 사용해서 검증오류 코드로 메시지 코드를 생성
* new FieldError() 를 생성하면서 메시지 코드들을 보관
* th:errors 에서 메시지 코드들로 메시지를 순서대로 찾고 노출

### 스프링이 기본적으로 만들어주는 에러메시지
스프링은 타입오류가 발생하면 typeMismatch 라는 오류코드를 사용한다. 이 오류코드가 MessageCodesResolver를 통하면서 
4가지 메시지코드가 생성된다
* typeMismatch.item.price
* typeMismatch.price
* typeMismatch.java.lang.Integer
* typeMismatch

실행하면 
```
default message [Failed to convert property value of type 'java.lang.String' to required type 'java.lang.Integer' for property 'price'; nested exception is java.lang.NumberFormatException: For input string: "ㅂㅂㅂ"]
```
해당하는 디폴트메시지가 뜨는데 마찬가지로 error.properties 에
```
typeMismatch.java.lang.Integer=숫자를 입력해주세요.
typeMismatch=타입 오류입니다.
```
를 추가하면 원하는 메시지로 설정할수있다. 


--- 
#  Validator 분리
스프링은 검증을 체계적으로 제공하기위해 다음 인터페이스를 제공한다.
```java
public interface Validator {
	boolean supports(Class<?> clazz);
	void validate(Object target, Errors errors);
}
```
* supports : 해당 검증기를 지원하는 여부 확인
* validate : 검증대상객체와 bindingResult

addItemV5 로 변경해보았다.
사실상 
```java 
public class ItemValidator implements Validator  
    //이부분에서 Validator 를 상속 안받아도 해당동작들은 잘수행될것이다 하지만 이다음 빈벨리데이션에서 그 이유가있다
``` 


### Validator 분리2
스프링의 Validator 인터페이스를 별도로 제공하는 이유는 체계적으로 검증기능을 도입하기위해서이다 그런데 앞에서는 검증기를 직접불러서 사용햇고, 이렇게사용해도된다.
그런데 Validator 인터페이스를 사용해서 검증기를 만들면 스프링의 추가도움을받을수있다

```java 
@InitBinder
    public void init(WebDataBinder dataBinder) {
        dataBinder.addValidators(itemValidator);
    }
```
이렇게 WebDataBinder 에 검증기를 추가하면 해당 컨트롤러에서는 검증기를 자동으로 적용할수있다.
@InitBinder 는 해당 컨트롤러에만 영향을 준다. 글로벌 설정은

```
WebMvcConfigurer 을 인터페이스받고 

@Override
public Validator getValidator() {
  return new ItemValidator();
} 
```
이렇게 적용하면 글로벌 설정이된다 글로벌설정도 아래작동방식과 같다.

이후 컨트롤러에 @Validated 어노테이션만 추가하면 검증을 자동으로 적용한다
-> 동작방식  
```
@Validated 는 검증기를 실행하라는 어노테이션이다.
이 어노테이션이 붙으면 앞서 WebDataBinder 에 등록한 검증기를 찾아서 실행한다. 그런데 여러 검증기를 등록한다면 
그중에 어떤 검증기가 실행되어야 할지 구분이 필요하다 이때 supports() 가 사용된다 여기서 supports(Item,class....) 가 호출되고
결과가 true 이므로 validate() 가 호출된다. 
```