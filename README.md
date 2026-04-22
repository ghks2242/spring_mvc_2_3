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