<?

require_once('../config.php');
require_once('lib/Ref.class.php');
require_once('lib/Translation.class.php');
$benchmark_start = microtime_float();

// arguments
$lang = isset($_POST['lang']) ? $_POST['lang'] : 'en';

// get translation file
//$translation = new Translation($lang);

// each lib
$libraries = array('net', 'serial', 'video', 'opengl', 'dxf', 'pdf');

$lib_dir = DISTDIR.'libraries';

//if (!is_dir(DISTDIR.'libraries/images')) { 
//	mkdir(DISTDIR.'libraries/images', '0757'); 
//}
//copydirr(CONTENTDIR."api_$lang/libraries/images", DISTDIR.'libraries/images');
//make_necessary_directories(DISTDIR.'libraries/images/include.php');
//copydirr(CONTENTDIR."api_$lang/LIB_images", DISTDIR.'libraries/images');

// get library index
$index = CONTENTDIR."api_$lang/libraries/index.html";
$page = new LocalPage('Libraries', 'Libraries', 'Libraries', '../');
$page->content(file_get_contents($index));
writeFile('distribution/libraries/index.html', $page->out());

// foreach lib
foreach ($libraries as $lib)
{
	$source = "api_$lang/LIB_$lib";
	$destination = "libraries/$lib";
	make_necessary_directories(DISTDIR.$destination.'/images/include');

    // get xml files
    //if (!$files = getXMLFiles(CONTENTDIR.$source)) { 
		//echo "couldn't open files"; 
	//} else {
	// parse xml files and create pages
	//    foreach ($files as $file)
	//	{
	//        $page = new LocalLibReferencePage(new Ref($source.'/'.$file), $lib, $translation, '../../');
	//        $page->write();
	//    }
	//}

    // template and copy index
    $index = CONTENTDIR.$source.'/index.html';
    $page = new LocalPage(ucfirst($lib) . ' \\ Libraries', 'Libraries', 'Libraries', '../../');
    $page->content(file_get_contents($index));
    writeFile('distribution/'.$destination.'/index.html', $page->out());
 
    // copy images directory
	//copydirr(CONTENTDIR.$source.'/images', DISTDIR.$destination.'/images');
}

if (!is_dir(DISTDIR.'libraries/images')) { 
	mkdir(DISTDIR.'libraries/images', '0757'); 
}
copydirr(CONTENTDIR."api_$lang/libraries/images", DISTDIR.'libraries/images');

$benchmark_end = microtime_float();
$execution_time = round($benchmark_end - $benchmark_start, 4);

?>

<h2>Library Generation Successful</h2>
<p>Generated files in <?=$execution_time?> seconds.</p>