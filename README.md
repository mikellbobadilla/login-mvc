# Guia de Spring Boot con SpringSecurity y MVC Thymeleaf

## Descripción del proyecto

Este proyecto es un ejemplo de como implementar el nuevo Spring Security, utilizando la autenticación por formulario usando Thymeleaf.

Solo está basado en como procesar las sesiones.

### Armando el proyecto

Para crear el proyecto se utilizó la página de Spring Initializer, con las siguientes dependencias:

La version de java de este proyecto es la 17.

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
spring.datasource.url=jdbc:mysql://localhost:3306/tech
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false # Si quieres que se muestren las consultas SQL lo pruebas con true
```

## Configuración de Spring Security

Anteriormente, la configuration de Spring Security se creaba una clase que heredaba de **WebSecurityConfigurerAdapter** en el que se tenía que sobreescribir algunos metódos.

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

    @Bean
    public SecurityFilerChain securityFilterChain(HttpSecurity http) throws Exception {
        // Rutas donde el usuario puede acceder y otras que no
    http
      .authorizeRequests(
        authorizeRequests -> authorizeRequests
          .antMatchers("/login", "/register", "/users").permitAll() // El usuario puede acceder al formulario de login

          .antMatchers("/assets/**").permitAll() // Para que se puedan aplicar los estilos de bootstrap 

          .anyRequest().authenticated() // Cualquier otra ruta que tengamos el usuario tiene que estar autenticado
    );

    // Configuración del login
    http.formLogin(
      formLogin -> formLogin
          .loginPage("/login") // Ruta de nuestro formulario custom de login (opcional) por defecto spring tiene su propio formulario de login
    );


    // Configuración del logout
    http.logout(
      logout -> logout
          .deleteCookies("JSESSIONID", "jwt") // Borra la cookie de sesión
          .logoutSuccessUrl("/login") // Url a la que se redirige en caso de exito
          .permitAll() // Permitir acceso a la página de logout a cualquiera
    );

    // Configuración de CSRF
    http.csrf().disable();

    return http.build();
    }
}
````

Por ahora la configuración que se está utilizando es la que vamos a usar para este proyecto, aunque vamos a agregar más.

## Creando un modelo de Usuario

```java
@Entity
@Table(name = "users") // Nombre que tendrá la tabla en la base de datos
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String username;
  private String password;

  public AppUser() {
  }

  public AppUser(final Long id, final String username, final String password) {
    this.id = id;
    this.username = username;
    this.password = password;
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }
}
```

El modelo nos servirá para poder mapear los usuarios en la base de datos y también **hibernate** nos creará la tabla con sus propiedades.

## Repositorio de Usuario

```java
@Repository
public interface UserRepository extends JpaRepository<AppUser, Long>{
  Optional<AppUser> findByUsername(String username); // Propiedad custom para encontrar un usuario por su username
  boolean existsByUsername(String username); // Propiedad custom que valida si existe el usuario por su username
}
```

La interfaz **UserRepository** es el que nos ayudara con la persistencia de los datos que recibamos del cliente.

## Páginas de HTML para el login y el registro de los usuarios

**Formulario de Login**

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="/assets/dist/css/bootstrap.min.css">
  <title>Inicio Session</title>
</head>
<body>
  <main class="container my-5" style="max-width: 500px">
    <h1 class="mt-5 mb-5 text-center">Inicio de Sesión</h1>

    <form class="row g-3" th:action="@{/login}" th:object="${user}" method="post">



      <div class="col-md-12 mb-2">
        <label for="username" class="">Usuario:</label>
        <input type="text" class="form-control border-2" id="username" th:field="*{username}" placeholder="tu-gatita.." required>
      </div>
  
      <div class="col-md-12 mb-2">
        <label for="password" class="">Contraseña:</label>
        <input type="password" class="form-control border-2" th:field="*{password}" id="password" placeholder="*****.." required>
      </div>
  
      <div class="d-grid gap-2 text-center">
        <p>¿No tienes cuenta? <a href="/register">create una!</a></p>
      </div>
  
      <div class="d-grid gap-2">
        <button type="submit" class="btn btn-primary">Iniciar session</button>
      </div>
      <p class="mt-5 mb-3 text-muted text-center">&copy; 22022 trying to live</p>
    </form>
  </main>

  <script src="/assets/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

**Formulario de Registro**

```html
<!DOCTYPE html>
 <html lang="en" xmlns:th="http://www.thymeleaf.org"> <!--donde se aplica thymeleaf -->
<head>
  <meta charset="UTF-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="/assets/dist/css/bootstrap.min.css">
  <title>Registro</title>
</head>
<body>
  <main class="container my-5" style="max-width: 500px">
    <h1 class="mt-5 mb-5 text-center">Creación de Usuario</h1>

    <form class="row g-3" th:action="@{/register}" th:object="${user}" method="post">

      <p th:if="${error}">
        <span th:text="${error}"></span>
      </p>

      <div class="col-md-12 mb-2">
        <label for="username" class="">Elija Usuario:</label>
        <input type="text" class="form-control border-2" id="username" th:field="*{username}" placeholder="tu-gatita.." required>
      </div>
  
      <div class="col-md-12 mb-2">
        <label for="password" class="">Elija Contraseña:</label>
        <input type="password" class="form-control border-2" th:field="*{password}" id="password" placeholder="*****.." required>
      </div>
  
      <div class="d-grid gap-2 text-center">
        <p>¿Ya tenes una cuenta? <a href="/login">Entrá!</a></p>
      </div>
  
      <div class="d-grid gap-2">
        <button type="submit" class="btn btn-primary">Crear Usuario</button>
      </div>
      <p class="mt-5 mb-3 text-muted text-center">&copy; 2022 trying to live</p>
    </form>
  </main>

  <script src="/assets/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

El formulario de **HTML** están adaptadas para que thymeleaf las pueda usar.

Los estilos de Bootstrap están en la carpeta **static** si los quieres aplicar a tu propio proyecto, también puedes usar un **cdn**

## Creación de los Servicios del Usuario

```java
@Service
public class UserService implements UserDetailsService{

  @Autowired
  UserRepository userRepository;

  BCryptPasswordEncoder passwordEncoder;

  // Carga al usuario por su nombre de usuario
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    AppUser user = userRepository.findByUsername(username).orElseThrow(
            () -> new UsernameNotFoundException(String.format("user %s not found", username))
    );

    return new User(user.getUsername(), user.getPassword(), new ArrayList<>());
  }

  // Válida si el usuario existe -> lo usaremos cuando un usuario se esté registrando
  public Boolean validateUsername (String username){
    return userRepository.existsByUsername(username);
  }

  // Crea un nuevo hash de password -> lo usaremos cuando un usuario se esté registrando
  public String encodePassword(String password){
    return passwordEncoder.encode(password);
  }
}
```

La clase **UserService** implementa la interfaz **UserDetailsService** el cual tenemos que implementar el método **loadUserByUsername** que recibe por parámetro el **usuario**, lo que hay dentro del método es la lógica básica para cargar un usuario.


## Antes de Crear los Controladores

Antes de crear los controladores hay que agregar algunas modificaciones en la clase **SecurityConfig** para poder gestionar los usuarios en spring

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired
  UserService userService; // Traemos un atributo de la clase UserService

  // Configuración de seguridad para la aplicación
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // Solo tienes que agregar a lo que ya se configuró.
    
    // Configuración de CSRF -> Está desactivado, ya que no se está usando en el formulario, pero si quieres lo puedes usar; pero tiene que modificar el formulario HTML
    http.csrf().disable();
    
    // EL proveedor que implementamos abajo
    http.authenticationProvider(authenticationProvider()); // Configuración del proveedor de autenticación

    return http.build();
  }

  // Configuración del proveedor de autenticación
  @Bean
  public DaoAuthenticationProvider authenticationProvider() throws Exception {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userService); // La clase user Service
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  // Configuración del AuthenticationManager -> procesa una petición de autenticación de una Request
  @Bean
  public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration configuration) throws Exception {
  return configuration.getAuthenticationManager();
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
  }
}
```
**SpringSecurity** maneja por si solo la autenticación cuando aplicamos esta configuración; solo que le estamos pasando las clases que queremos que utilize **spring** para autenticar un usuario.

La configuración de ahora **spring** guarda en memoria el usuario que están autenticados y les da acceso a esas rutas.

La autenticación con **JSON web Token** se implementará más adelante, si no quires guardar en memoria los usuarios que están autenticados.


## Creando los Controladores

```java
@Controller
public class UserController {
  
  @Autowired // Agrega la clase directamente en el atributo, de otra manera hay que crear su constructor al UserController pasando por parámetro los atributos declarados
  UserRepository userRepository;

  @Autowired
  BCryptPasswordEncoder passwordEncoder;
    
  @Autowired
  UserService userService;

  @GetMapping("/")
  public String getHello() {
    return "index";
  }

  @GetMapping("/login")
  public String getLogin(Model model) {
    AppUser user = new AppUser();

    model.addAttribute("user", user);
  
    return "login";
  }

  @GetMapping("/register")
  public String getRegister(Model model) {
    AppUser user = new AppUser();

    model.addAttribute("user", user);
  
    return "register";
  }

  @PostMapping("/register")
  public String postRegister(@ModelAttribute AppUser user, Model model, HttpServletResponse response) {
    boolean userExist = userRepository.existsByUsername(user.getUsername());
    if (userExist) {
      return "redirect:/register";
    }
    String encoded = passwordEncoder.encode(user.getPassword());
    AppUser newUser = new AppUser(null, user.getUsername(), encoded);
    userRepository.save(newUser);
    return "redirect:/users";
  }
}
```