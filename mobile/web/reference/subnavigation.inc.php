<?php
$section = -1;
if (stristr($_SERVER['PHP_SELF'], 
            SITE_ROOT .'reference/index.php') !== false) {
    $section = 0;
} else if (stristr($_SERVER['PHP_SELF'], 
                   SITE_ROOT .'reference/environment/index.php') !== false) {
    $section = 1;
} else if (stristr($_SERVER['PHP_SELF'], 
                   SITE_ROOT .'reference/libraries/index.php') !== false) {
    $section = 2;
}
?>

<div id="subnavigation" style="padding-left: 208px">
    <img src="<?php echo SITE_ROOT?>images/nav_bottomarrow.png" align="absmiddle">

<?php if ($section == 0) { ?>
    Language
<?php } else { ?>
    <a href="<?php echo SITE_ROOT ?>reference/index.php">Language</a>
<?php } ?>

    <span class="backslash">\</span>
<?php if ($section == 1) { ?>
    Environment
<?php } else { ?>
    <a href="<?php echo SITE_ROOT ?>reference/environment/index.php">Environment</a> 
<?php } ?>

    <span class="backslash">\</span>
<?php if ($section == 2) { ?>
    Libraries
<?php } else { ?>
    <a href="<?php echo SITE_ROOT ?>reference/libraries/index.php">Libraries</a>
<?php } ?>

</div>
