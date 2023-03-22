package eu.geekplace.beanstalk

class BuildConstants {
	static final ENABLE_PREVIEW = "--enable-preview"
	static final UNRESTRICT_CONTENDED = "-XX:-RestrictContended"
	static final EXPORT_JDK_INTERNAL_VM_ANNOTATION = [
		"--add-exports", "java.base/jdk.internal.vm.annotation=eu.geekplace.beanstalk.core"
	]

	static final EXTRA_JAVAC_ARGS = EXPORT_JDK_INTERNAL_VM_ANNOTATION + [
		ENABLE_PREVIEW,
	]

	static final EXTRA_JVM_ARGS = EXTRA_JAVAC_ARGS + [
		UNRESTRICT_CONTENDED,
	]
}
