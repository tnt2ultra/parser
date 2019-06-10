<!DOCTYPE html>
<!--
@author Anri Started: 2019-06-10
-->
<html>
    <head>
        <meta charset="UTF-8">
        <title>Страница c постраничным выводом списка загруженных файлов 
            и возможностью поиска по точному наименованию файла</title>
    </head>
    <body>
        <?php
        $dir = './downloads/';  // Путь к папке с файлами        
        $COUNT_FILES_ON_PAGE = 100; // Кол-во файлов на странице

        $p = filter_input(INPUT_GET, 'p');
        if (empty($p) || $p < 0 || !intval($p)) {
            $p = 0;
        }
        $S = "";
        $SUBSTR = filter_input(INPUT_GET, 's');
        if (!empty($SUBSTR)) {
            $S = "s=" . myUrlEncode($SUBSTR) . "&";
        }
        $handle = opendir($dir);
        $num = 0;
        if ($handle) {
            while (false !== ($file = readdir($handle))) {
                if ($file != "." && $file != ".." && (empty($SUBSTR) 
                        || (mb_strpos($file, $SUBSTR, 0, 'UTF-8') !== false))) {
                    $files[] = $file;
                    $num = $num + 1;
                    if ($num > (($p + 1) * $COUNT_FILES_ON_PAGE)) {
                        break;
                    }
                }
            }
            closedir($handle);
        }

        echo PHP_EOL;
        echo '<b>Страница c постраничным выводом списка загруженных файлов и '
        . 'возможностью поиска по точному наименованию файла</b><br>' . PHP_EOL;
        echo '<form method="get" action="">' . PHP_EOL;
        echo '<input type="text" name="s" size="20" maxlength="100" value="'
        . $SUBSTR . '"><br>' . PHP_EOL;
        echo '<input type="submit" value="Подстрока в названии файла">' . PHP_EOL;
        echo '</form>' . PHP_EOL;

        if ($num == 0) {
            echo 'Папка "' . $dir . '" пустая.<br>' . PHP_EOL;
            if (!empty($SUBSTR)) {
                echo 'Или подстрока "' . $SUBSTR 
                        . '" не встречается в названиях файлов.<br>' . PHP_EOL;
            }
        } else {
            $COUNT_FILES = count($files);
            $COUNT_PAGES = intval($COUNT_FILES / $COUNT_FILES_ON_PAGE);
            if ($COUNT_FILES_ON_PAGE * $COUNT_PAGES == $COUNT_FILES) {
                $COUNT_PAGES = $COUNT_PAGES - 1;
            }
            if ($p > $COUNT_PAGES) {
                $p = $COUNT_PAGES;
            }

            echo 'Файлов на страницу: <b>' . $COUNT_FILES_ON_PAGE . '</b><br>' 
                    . PHP_EOL;
            echo 'Текущая страница: <b>' . ($p + 1) . '</b><br>' . PHP_EOL;

            for ($i = 0; $i <= $COUNT_PAGES; $i++) {
                echo '<a href="?' . $S . 'p=' . $i . '">[' . ($i + 1) . ']</a> ' 
                        . PHP_EOL;
            }
            echo '<hr>' . PHP_EOL;

            $START = $p * $COUNT_FILES_ON_PAGE;
            $END = $START + $COUNT_FILES_ON_PAGE;
            for ($i = $START; $i < $END; $i++) {
                if (key_exists($i, $files)) {
                    echo $files[$i] . '<br>' . PHP_EOL;
                }
            }

            echo '<hr>' . PHP_EOL;
            if ($p > 0) {
                echo '<button><a href="?' 
                        . $S . 'p=0">В начало</a></button>' . PHP_EOL;
                echo '<button><a href="?' . $S . 'p=' 
                        . ($p - 1) . '">Назад</a></button>' . PHP_EOL;
            } else {
                echo '<button disabled>В начало</button>' . PHP_EOL;
                echo '<button disabled>Назад</button>' . PHP_EOL;
            }
            if ($p < $COUNT_PAGES) {
                echo '<button><a href="?' . $S . 'p=' 
                        . ($p + 1) . '">Вперед</a></button>' . PHP_EOL;
            } else {
                echo '<button disabled>Вперед</button>' . PHP_EOL;
            }
            echo PHP_EOL;
        }

        function myUrlEncode($string) {
            $entities = array('%21', '%2A', '%27', '%28', '%29', '%3B', '%3A', '%40', 
                '%26', '%3D', '%2B', '%24', '%2C', '%2F', '%3F', '%25', '%23', '%5B', '%5D');
            $replacements = array('!', '*', "'", "(", ")", ";", ":", "@", "&", "=", 
                "+", "$", ",", "/", "?", "%", "#", "[", "]");
            return str_replace($entities, $replacements, urlencode($string));
        }
        ?>
    </body>
</html>
