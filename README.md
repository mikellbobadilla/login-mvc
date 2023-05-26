# Guia de Spring Boot con SpringSecurity y MVC Thymeleaf

## Descripción del proyecto

Este proyecto es un ejemplo de como implementar el nuevo Spring Security, utilizando la autenticación por formulario usando Thymeleaf.

Solo está basado en como procesar las sesiones.

### Armando el proyecto

Para crear el proyecto se utilizó la página de Spring Initializer, con las siguientes dependencias:

La version de java de este proyecto es la 17, pero puedes usar la versión que quieras.

**Spring Web**: Para crear el proyecto web
**Spring Security**: Para la seguridad
**Spring Data JPA**: Para la persistencia de datos
**Thymeleaf**: Para la creación de las vistas
**MySQL Driver**: Para la conexión con la base de datos
**Spring Boot DevTools**: para que se actualice automáticamente el proyecto al guardar los cambios

### Configuración de la base de datos

Para la configuración de la base de datos se utilizó MySQL, para ello se debe crear una base de datos llamada **tech** y ejecutar el script que se encuentra en la carpeta **resources** del proyecto.

En la carpeta **resources** se encuentra el archivo **application.properties** donde se configura la conexión con la base de datos.

Ejemplo:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/login
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false # Si quieres que se muestren las consultas SQL lo pruebas con true
```

## Configuración de Spring Security

Anteriormente, la configuration de Spring Security se creaba una clase que heredaba de **WebSecurityConfigureAdapter** en el que se tenía que sobreescribir algunos metódos.

ejemplo:

```java
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http.authorizeRequests()
          .antMatchers("/login").permitAll() // Cualquier usuario puede acceder a la ruta /login
          .anyRequest().authenticated(); // Cualquier otra ruta requiere autenticación
    }
}
```

Ahora en la version de Spring Security 5.7.0-M2 la clase **WebSecurityConfigurerAdapter** fue deprecada, por lo tanto, la nueva configuración sería esta.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final AuthenticationProvider authProvider;

  public SecurityConfig(AuthenticationProvider authProvider) {
    this.authProvider = authProvider;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
      .formLogin()
      /* Referencia al controlador que responde el formulario de login */
      .loginPage("/login")
      .and()
      /* El tipo de autenticación se configuró. Está dentro de la clase 'AppConfig' */
      .authenticationProvider(authProvider)
      .authorizeHttpRequests()
      /* Url que se puede acceder sin estar autenticado */
      .antMatchers("/login", "/register")
      .permitAll()
      /* El resto de los endpoints requiere autenticación */
      .anyRequest()
      .authenticated();


    return http.build();
  }
}
```

## Configuración de la Autenticación a nivel Aplicación

```java
@Configuration
public class AppConfig {

  private final UserRepository userRepository;

  public AppConfig(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder(10);
  }

  /*
    Se utilizó el término de clase anónima, porque la clase 'UserDetailsService' solo tiene un método,
    que es 'loadByUsername()' 
  */
  @Bean
  public UserDetailsService userDetailsService(){
    /*
      Se usó lambda para que sea más legible
    */
    return username -> userRepository.findByUsername(username)
       .orElseThrow(()-> new UsernameNotFoundException("El usuario no existe!"));
  }

  /* El proveedor que usará Spring para la autenticación, se pueden agregar más tipos de autenticación */
  @Bean
  public AuthenticationProvider authProvider(){
    var provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService());
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authManager(AuthenticationConfiguration auth) throws Exception {
    return auth.getAuthenticationManager();
  }
}
```

Esta es la configuración básica para que **Spring** pueda manejar la autenticación.

**SpringSecurity** maneja por si solo la autenticación cuando aplicamos esta configuración; solo que le estamos pasando las clases que queremos que utilize **spring** para autenticar un usuario.

## Creando un modelo de Usuario

```java

@Entity
@Table(name = "users")
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  @Column(unique = true)
  private String username;
  private String password;

  public User(String name, String username, String password) {
    this.name = name;
    this.username = username;
    this.password = password;
  }

  public User() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  /* Como no se configuró roles, no es necesario configurarlo */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return null;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}

```

La clase 'User' implementa la interfaz 'UserDetails', porque **Spring** encapsula dentro de una autenticación.
El modelo nos servirá para poder mapear los usuarios en la base de datos y también **Hibernate** nos creará la tabla con sus propiedades.

## Repositorio de Usuario

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long>{
  Optional<AppUser> findByUsername(String username); // Propiedad custom para encontrar un usuario por su username
}
```

La interfaz **UserRepository** es el que nos ayudara con la persistencia de los datos que recibamos del cliente.

## Creando DTO para parametrizar los datos obtenidos

Primero debemos crear una clase para recibir los datos del usuario que recibimos del formulario.

**Para las peticiones de Registro**
```java

public class RegisterRequest {
  private String username;
  private String password;
  private String name;

  public RegisterRequest(String username, String password, String name) {
    this.username = username;
    this.password = password;
    this.name = name;
  }

  public RegisterRequest() {
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}

```

**Petición de Autenticación**
```java

public class AuthRequest {
  private String username;
  private String password;

  public AuthRequest(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public AuthRequest() {
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}

```

Estas clases las utiliza **Thymeleaf**, creando los objetos con los datos del usuario que recibimos.
Dentro del controlador entenderemos su uso.

## Páginas de HTML para el login y el registro de los usuarios

**Formulario de Login**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.w3.org/1999/xhtml" xmlns:sf="http://www.w3.org/1999/xhtml" lang="es">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
    <title>Inicio Sesión</title>
    <style>
        html,
        body {
            height: 100%;
        }

        body {
            display: grid;
            place-items: center;
            margin: 0;
            padding: 0 32px;
            animation: rotate 6s infinite alternate linear;
        }

        form {
            max-width: 400px;
        }
    </style>
</head>
<body class="bg-dark text-light">
<form class="row g-3" th:action="@{/login}" th:object="${authRequest}" method="post">

    <div th:if="${message}">
        <p class="text-success text-center" th:text="${message}"></p>
    </div>
    
    <div th:if="${param.error}">
        <p class="text-danger text-center" th:text="'Los datos son incorrectos'"></p>
    </div>

    <h2 class="fs-1 text-center">Login</h2>

    <div class="col-md-12 mb-2">
        <label for="username" class="py-1">Usuario:</label>
        <input type="text" class="form-control border-2 bg-dark text-light shadow-none" id="username"
               th:field="*{username}" placeholder="user.." required>
    </div>

    <div class="col-md-12 mb-2 bg-dark">
        <label for="password" class="py-1">Contraseña:</label>
        <input type="password" class="form-control border-2 bg-dark text-light shadow-none" th:field="*{password}"
               id="password" placeholder="pass123.." required>
    </div>

    <div class="d-grid gap-2 text-center">
        <p>¿No tienes cuenta? <a href="/register">create una!</a></p>
    </div>

    <div class="d-grid gap-2">
        <button type="submit" class="btn btn-primary">Iniciar session</button>
    </div>
    <p class="mt-5 text-muted text-center">
        &copy; 2023 mikell bobadilla<br />
        <strong><a class="text-muted" href="https://github.com/mikellbobadilla/login_thymeleaf" target="_blank">Link to project</a></strong>
    </p>
</form>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM"
        crossorigin="anonymous"></script>
</body>
</html>
```

**Formulario de Registro**

```html

<!DOCTYPE html>
<html xmlns:th="http://www.w3.org/1999/xhtml" xmlns:sf="http://www.w3.org/1999/xhtml" lang="es">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
    <title>Registro</title>
    <style>
    html,
    body {
      height: 100%;
    }

    body {
      display: grid;
      place-items: center;
      margin: 0;
      padding: 0 32px;
      animation: rotate 6s infinite alternate linear;
    }

    form {
      max-width: 400px;
    }
  </style>
</head>
<body class="bg-dark text-light">
<form class="row g-3" th:action="@{/register}" th:object="${registerRequest}" method="post">

    <div th:if="${message}">
        <p class="text-warning text-center" th:text="${message}">Algun Texto</p>
    </div>

    <h2 class="fs-1 text-center">Registro</h2>

    <div class="col-md-12 mb-2">
        <label for="username" class="py-1">Usuario:</label>
        <input type="text" class="form-control border-2 bg-dark text-light shadow-none" id="username"
               th:field="*{username}" placeholder="user.." required>
    </div>

    <div class="col-md-12 mb-2">
        <label for="name" class="py-1">Nombre:</label>
        <input type="text" class="form-control border-2 bg-dark text-light shadow-none" id="name" th:field="*{name}"
               placeholder="name.." required>
    </div>

    <div class="col-md-12 mb-2 bg-dark">
        <label for="password" class="py-1">Contraseña:</label>
        <input type="password" class="form-control border-2 bg-dark text-light shadow-none" th:field="*{password}"
               id="password" placeholder="pass123.." required>
    </div>

    <div class="d-grid gap-2 text-center">
        <p>¿Ya tenes cuenta? <a href="/login">entra!</a></p>
    </div>

    <div class="d-grid gap-2">
        <button type="submit" class="btn btn-primary">Registrarme</button>
    </div>
    <p class="mt-5 text-muted text-center">
        &copy; 2023 mikell bobadilla<br/>
        <strong><a class="text-muted" href="https://github.com/mikellbobadilla/login_thymeleaf" target="_blank">Link to project</a></strong>
    </p>
</form>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM"
        crossorigin="anonymous"></script>
</body>
</html>

```

El formulario de **HTML** están adaptadas para que thymeleaf las pueda usar.

## Crear el Servicio de Autenticación

```java
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(RegisterRequest request){
        User user = new User(
                request.getName(),
                request.getUsername(),
                passwordEncoder.encode(request.getPassword())
        );

        userRepository.save(user);
        return "Usuario creado!";
    }
}
```

## Creando los Controladores

```java
@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(Model model){
        AuthRequest auth = new AuthRequest();
        model.addAttribute("authRequest", auth);
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model){
        RegisterRequest register = new RegisterRequest();
        model.addAttribute("registerRequest", register);
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest request, RedirectAttributes attributes){
        String message = authService.register(request);
        attributes.addFlashAttribute("message", message);
        return "redirect:/login";
    }

    /* Manejo de Errores, si el usuario ya está registrado */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public String errorRegister(RedirectAttributes attributes, SQLIntegrityConstraintViolationException exc){
        Logger.getLogger(AuthController.class.getName()).warning(exc.getMessage());
        attributes.addFlashAttribute("message", "El usuario ya existe");
        return "redirect:/register";
    }
}

```