# Creational (Порождающие)

## Factory Method

### Вариант I:
**Factory Constructor** - статический метод, замещающий конструктор (может содержат дополнительное поведение)

*Пример из жизни:* [Bitmap.createBitmap()](https://developer.android.com/reference/android/graphics/Bitmap#createBitmap(android.graphics.Bitmap,%20int,%20int,%20int,%20int)), создающий новую картинку из старой с другим размером

### Вариант II:
Отдельный класс (или абстракция) Creator с **ЕДИНСТВЕННЫМ** статическим, либо с обычным методом, возвращающий разновидность объектов (абстракцию) либо дженерик

*Пример из жизни 1:* Создание Fragment в Android с аргументами

```Java
@NonNull
public static GameFragment newInstance(
    final @NonNull PlayerType playerType,
    final @NonNull PlayerRole playerRole
) {
    final GameFragment fragment = new GameFragment();
    final Bundle args = new Bundle();

    args.putInt(PLAYER_ROLE_KEY, playerRole.ordinal());
    args.putInt(PLAYER_TYPE_KEY, playerType.ordinal());

    fragment.setArguments(args);
    return fragment;
}
```

*Пример из жизни 1:* Реализация [Parcelable](https://developer.android.com/reference/android/os/Parcelable) в Android

```Kotlin
data class Message(
    val id: Int,
    val fromUserId: Long,
    val toUserId: Long,
    val text: String,
    val sendTime: LocalDateTime,
    val read: Boolean
) : Parcelable {
    companion object CREATOR : Parcelable.Creator<DBMessage> {
        override fun createFromParcel(parcel: Parcel) = DBMessage(parcel)
        override fun newArray(size: Int) = arrayOfNulls<DBMessage?>(size)
    }

    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        fromUserId = parcel.readLong(),
        toUserId = parcel.readLong(),
        text = parcel.readString() ?: "",
        sendTime = parcel.readLocalDateTime(),
        read = parcel.readInt() > 0
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeLong(fromUserId)
        parcel.writeLong(toUserId)
        parcel.writeString(text)
        parcel.writeLocalDateTime(sendTime)
        parcel.writeInt(if (read) 1 else 0)
    }

    override fun describeContents() = 0
}
```

## Abstract Factory

1. Абстрактная фабрика создающая абстрактные продукты
2. Конкретные продукты, реализующие абстрактные
3. Конкретные фабрики, реализующие абстрактные, создающие конкретные объекты с интерфейсом абстрактных

### Реализация:
1. Отдельная абстракция "Абстрактная фабрика" с методом create(Factory, Dependency...): Entity
2. Подфабрики (реализуются через фабричный метод)
3. Создаваемые сущности (ConcreteEntity) и их абстракция (Entity)

*Пример из жизни 1:* [ViewModelProvider.Factory](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories#kotlin) для создания ViewModel-и с необходимыми зависимостями

*Пример из жизни 2:* [FragmentFactory](https://developer.android.com/reference/kotlin/androidx/fragment/app/FragmentFactory) для создания Fragment-ов с зависимостями

```Kotlin
class GlobalFragmentFactory @Inject constructor() : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        if (className == MainFragment::class.java.name)
            return MainFragment()

        if (className == SimpleNotesFragment::class.java.name)
            return SimpleNotesFragment()

        if (className == DatedNotesFragment::class.java.name)
            return DatedNotesFragment()

        return super.instantiate(classLoader, className)
    }
}
```

## Builder

### Вариант I:

1. Некий базовый объект, который может иметь дефолтные параметры
2. Внутренний класс, который конструирует этот объект

*Пример из жизни:*

```Kotlin
private fun NotificationBuilder(context: Context, note: DatedNote.Entity) =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
            Notification.Builder(context.applicationContext, ALARM_CHANNEL_ID)
        else -> Notification.Builder(context.applicationContext)
    }
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle(note.title)
        .setContentText(context.resources.getString(R.string.time_s_app))
        .setOngoing(false)
        .setOnlyAlertOnce(true)
        .setStyle(
            Notification.BigTextStyle().bigText(
                DefaultMarkwon(context).toMarkdown(note.description)
            )
        )
        .setContentIntent(
            PendingIntent.getActivity(
                context.applicationContext,
                note.hashCode(),
                Intent(context.applicationContext, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

...

notificationManager.notify(
    NOTIFICATION_ID,
    NotificationBuilder(context, note).build()
)
```

### Вариант II (по Банде 4-ех):

1. Абстрактный класс, строящий в отдельном методе итоговый продукт, отдельно его зависмости
2. Метод getProduct(), возвращающий текущий объект на момент крепления к нему всех зависимостей
(Пример с лабиринтом, дверьми, комнатами и стенами)

*Note:* отличается от AF тем, что строитель пошагово конструирует объект и позволяет его дополнить в процессе, тогда как AF выдает готовый результат

### Prototype

Трейт Prototype или Clone, клонирующий объект с помощью метода clone()

*Пример из жизни:* [Clone](https://doc.rust-lang.org/std/clone/trait.Clone.html) в Rust

## Singleton

Инициализирует единственную статическую сущность объекта в статическом методе, запрещает создание новых сущностей. Весь функционал работает только через Singleton::get_instance().method_call()

*Пример из жизни 1*: object в Kotlin

```Kotlin
object MyObject {
    val property = "My Property"
}

println(MyObject.proprty)
```

*Пример из жизни 2:*

Реализация паттерна [Репозиторий](https://habr.com/ru/articles/248505/)

```Kotlin
class ChatRepository(context: Context) {
    companion object {
        private const val DATABASE_NAME = "chat"
        private var INSTANCE: ChatRepository? = null

        @Synchronized // потокобезопасность
        fun getInstance(context: Context): ChatRepository {
            if (INSTANCE == null) INSTANCE = ChatRepository(context)
            return INSTANCE!!
        }
    }

    private val db = Room.databaseBuilder(
        context,
        ChatDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val usersDao = db.usersDao()

    suspend fun getUserByJobId(jobId: Long) =
        usersDao.getUserByJobId(jobId)
}

ChatRepository.getInstance().getUserJobById(jobId = someId)
```

*Note:* не стоит говорить о нем в самом начале во время собеседования...

# Structural

## Adapter (Wrapper)

### Вариант I
Оборачивает один класс и реализует другой класс (пример: обернуть треугольник в TriangularSquare и реализовать Square трейт)

```Kotlin
class TriangularSquare(private val triangle: Triangle) : Square
```

### Вариант II:
Множественное наследование в плюсах

```C++
class triangular_square : public square, private triangle {}
```

## Bridge

Отделить абстракцию от реализации, чтобы изменять обе независимо друг от друга

1. Абстракция, хранящаяя ссылку на абстрактную реализацию и абстрактный метод get_impl()
2. Абстрактная реализация **НЕ РЕАЛИЗУЮЩАЯ** абстракцию (т.е. нет extends, implements и т.п.)
3. Конкретные классы, реализующие обе абстракции

*Пример из реальной жизни:* кодогенерация через рефлексию (e.g. реализация Data-Access-Object в ORM)

## Composite (Компоновщик)

Для создания древовидных структур (пример: меню, подменю, действия)

**Структура:**

1. Абстракция Component, которую все реализуют
2. Leaf : Component, у которого не будет дочерних компонент
3. Composite : Component, у которого есть дочерние компоненты (хранятся в некой структуре данных)

## Decorator

Динамически добавляет объекту новые свойства

1. Abstraction
2. Concrete : Abstraction
3. AbstractDecorator(Abstraction) : Abstraction
4. ConcreteDecorator(Abstraction) : AbstractDecorator(Abstraction) { fun decoratorFeature() }

*Пример из реальной жизни:* реализация итераторов, sequence, stream и подобных ленивых функциональных единиц

```Kotlin
sourceCollection.asSequence()
    .map { it + 1 }
    .map { it + 2 }
    .map { it + 3 }
    .toList()

// под капотом

val resultIterator = 
    MapIterator( { it + 3 }, 
        MapIterator( { it + 2 }, 
            MapIterator( { it + 1}, 
                sourceCollection.iterator()
            )
        )
    )

val result = mutableListOf<Int>()
resultIterator.forEach(result::add)

```

## Facade

Ослабить использование объектов другими объектами путем выделения объекта-фасада, который всем управляет

1. Независимые компоненты, которые делают свое дело и не хранят ссылок **НИ на другие компоненты, НИ НА САМ ФАСАД**

2. Фасад, который объединяет работу всех компонентов, словно дирижер

*Note:* отличается от Mediator-a тем, что **отношения односторонние**

## Flyweight (Приспособленец)

*Фабрика одиночек*

Помогает сократить расходы памяти, если объектов слишком много, путем их кеширования

1. Объект, использование которого хотим кешировать
2. Объект со словарем уникальных объектов, где по ключу мы либо достаем существующий объект, либо хешируем в словаре

*Пример из реальной жизни:* Создание сущностей-одиночек в Dependency Injection фреймворках

## Proxy/Surrogate/Placeholder (Заместитель)

Замещает тяжелый в создании и инициализации объект, пока может (пример: картинка и метаданные)

1. Трейт с методами
2. Конкретный объект, создать который достаточно ресурсоемко
3. Proxy, который может заместить конкретный объект на некоторых запросах (например, метаданные)

*Пример из реальной жизни:* загрузка картинок из сети (бибилиотеки Glide, Coil, Picasso и т.п.):

```Kotlin
@Composable
fun HumanPlaceholder(
    tint: Color,
    contentDescription: String? = stringResource(id = R.string.profile)
) = Icon(
    painter = painterResource(id = R.drawable.profile_icon),
    contentDescription = contentDescription,
    tint = tint
)

SubcomposeAsyncImage(
    contentDescription = stringResource(id = R.string.profile),
    contentScale = ContentScale.FillBounds,
    alignment = Alignment.Center,
    modifier = modifier.clip(CircleShape),
    loading = { HumanPlaceholder(tint = colors.primary) },
    error = { HumanPlaceholder(tint = colors.primary) },
    model = ImageRequest.Builder(context)
        .data(employee.avatarUrl)
        .networkCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .precision(Precision.EXACT)
        .scale(Scale.FILL)
        .crossfade(400)
        .build(),
    )
```

# Behaviour

## Chain of Responsibility

Перекладывание ответственности по дереву (цепи)

1. Трейт с функционалом, который будет отправлен по цепочке до корня (или раньше) (e.g. RequestHandler с методом handleRequest())
2. Иерархическая структура, где все реализуют этот трейт
3. Компонент иерархии, запуская handleRequest(), вызывает предка, который запускает свой handleHelp() и переходит к своему предку и так далее, пока не дойдем до нужного компонента

## Command/Action/Transaction

Обертка над callback-ом (функцией)

1. Абстракция с методом запуска (execute/invoke/run и т.п.) и отмены (unexecute, cancel и т.п.)
2. Конкретные объекты с реализацией метода
3. (Опционально) Макро-комманды, использующие под-комманды

*Легендарный Пример из реальной жизни:* [AsyncTask](https://developer.android.com/reference/android/os/AsyncTask)

```Java
class DownloadFilesTask extends AsyncTask<URL, Integer, Long> {
    protected Long doInBackground(URL... urls) {
        int count = urls.length;
        long totalSize = 0;

         for (int i = 0; i < count; i++) {
             totalSize += Downloader.downloadFile(urls[i]);
             publishProgress((int) ((i / (float) count) * 100));

             if (isCancelled())
               break;
         }

         return totalSize;
     }

     protected void onProgressUpdate(Integer... progress) {
         setProgressPercent(progress[0]);
     }

     protected void onPostExecute(Long result) {
         showDialog("Downloaded " + result + " bytes");
     }
}

new DownloadFilesTask().execute(url1, url2, url3)
```

*Пример из жизни 2:* Callback-и в Джаве + @FunctionalInterface

## Interpreter

Парсинг и обработка языков

1. Глобальный Context - парсит и осмысливает выражения
2. AbstractExpression с методом interpert()
3. TerminalExpression - простое однозначное выражение (не вложено, например, символы, скобки, и тд)
4. NonTerminalExpression - вложенные рекурсивные выражения (хранит ссылки на дочерние AbstractExpression)

## Iterator

Позволяет обходить структуру данных в соответствии с алгоритмом поска следующего/предыдущего элемента.

1. Структура данных
2. Iterator с методом next(), get() и другими опциональными методами

## Mediator/Director (Посредник)

Ослабляет ссылаемость объектов друг на друга, становлясь между ними

**Структура:**

1. Mediator/Director - определяет интерфейс взаимодействия между объектами, хранит на всех ссылки
2. Collegue - хранят ссылку ТОЛЬКО на Mediator, общаются только через него

*Пример из реальной жизни:* [Model-View-ViewModel (MVVM)](https://ru.wikipedia.org/wiki/Model-View-ViewModel) архитектура

![MVVM](https://www.lexone.ru/storage/2022/01/Screenshot-2022-01-10-at-23.23.22.png)

*Note:* отличается от Фасада тем, что отношения между посредником и коллегами **двухсторонние**

**Memento/Snapshot (Хранитель/Снимок)**

Сохранение и откат состояний, не нарушая инкапсуляции

**Структура:**

1. State - хранит некое состояние программы
2. Memento - хранит информацию о снимке, приватно для Originator-а позволяет переназначить состояние и получить его
3. Caretaker (Опекун) - сохраняет предыдущие состояния
4. Originator (Создаетль) - объект, состояния которого сохраняет опекун. Хранит текущее состояние, создает новые снимки createMemento() и обновляет текущее состояние через снимок setMemento()

*Пример из реальной жизни:* система коммитов Git

## Observer

Обновляет подписчиков, посылая сигналы

**Структура:**

1. AbstractSubject - абстракция, хранящая Option<Observer> c методами
 subscribe(Observer) { this.observer = observer; observer.addSubscriber(this) },  
 unsubscribe(Observer) { this.observer = none; observer.removeSubscriber(this) },
 notify() { observer.update() }

2. ConcreteSubject - реализует AbstractSubject и хранит свои состояния и методы
   
3. Observer - хранит список подписчиков и обновляет их после дочернего вызова subscriber.notify()

*Пример из реальной жизни:* парадигма реактивного программирования

```Kotlin
@Composable
fun TasksScreen(
    tasksViewModel: TasksViewModel,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val tabIndex = pagerState.currentPage // реактивно обновляется под капотом, вызывая рекомпозицию функции

    // Реактивные состояния
    val incomingTasks by tasksViewModel.incomingTasks.collectAsState()
    val executingTasks by tasksViewModel.executingTasks.collectAsState()

    // Обновление состояний через поллинг
    DisposableEffect(Unit) {
        val task = tasksViewModel.viewModelScope.launch {
            while (true) {
                tasksViewModel.loadIncomingTasks()
                tasksViewModel.loadExecutingTasks()
                delay(1000)
            }
        }

        onDispose { task.cancel() }
    }

    HorizontalPager(state = pagerState, modifier = modifier) { page ->
        when (page) {
            0 -> TaskList(
                tasks = incomingTasks, // вызовет рекомпозицию списка задач при обновлении
                tasksViewModel = tasksViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp)
            )

            1 -> TaskList(
                tasks = executingTasks, // вызовет рекомпозицию списка задач при обновлении
                tasksViewModel = tasksViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp)
            )
        }
    }
```

## State

Выполнить блок кода, если случилось некое условие (заменить if/else)

**Структура:**

1. Трейт State с методом handle/execute
2. Конкретные состояния с реализацией метода handle/execute
3. Context - запускает состояния (через handle), если условие запуска наступает

*Пример из реальной жизни:* жизненный цикл [Activity](https://developer.android.com/guide/components/activities/activity-lifecycle)

![activity_lifecycle](https://developer.android.com/guide/components/images/activity_lifecycle.png)

## Strategy

Структурирует схожие по назначению алгоритмы внутри отдельных объектов

**Структура:**

1. Трейт Strategy - запустит алгоритм-метод с некими аргументами, одинаковыми для всех стратегий
2. ConcreteStrategy - реализует Strategy и запускает конкретный алгоритм
1. Context/Composition - хранит ссылку на абстрактную стратегию и запускает ее

*Пример из реальной жизни:* библиотека [Landscapist](https://github.com/skydoves/landscapist), объединяющая несколько разных библиотек асинхронной загрузки картинок общим интерфейсом 

## Template Method

Абстракция с дефолтной реализацией функционала, которую нельзя переопределить

**Структура:**

1. AbstractObject с final doDefaultBreakfast() { варить_каша(); пить_чай(); }
2. Конкретный объект, использующий doDefault(), если нужно

## Visitor

Объединяет схожие алгоритмы объектов, не связанных друг с другом никаким родством

**Структура:**

1. Visitor { visit(ElementA); visit(ElementB) }, оба visit-а выполняют схожие по смыслу действия
2. Конкретные ElementA и ElementB, не связанные друг с другом достаточно сильно, чтобы использовать visit(AbstractElement)
