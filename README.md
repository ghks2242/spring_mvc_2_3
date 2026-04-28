검증
---
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